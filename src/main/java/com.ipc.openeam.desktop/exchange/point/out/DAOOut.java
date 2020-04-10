package com.ipc.openeam.desktop.exchange.point.out;

import com.google.inject.Inject;
import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.BasicBeanWithClass;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.field.FieldValue;
import com.ipc.openeam.desktop.dao.*;

import com.ipc.openeam.desktop.bean.attribute.AttributeOnClassstructure;
import com.ipc.openeam.desktop.bean.attribute.AttributeValue;
import com.ipc.openeam.desktop.bean.classification.Classstructure;

import com.ipc.openeam.desktop.exchange.AbstractOut;
import com.ipc.openeam.desktop.exchange.AbstractSystem;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DAOOut extends AbstractOut {
	private static Logger log = Logger.getLogger(DAOOut.class);
	private Map<String, Classstructure> classMapping = new HashMap<>();
	private Map<Class<? extends BasicBean>, Map<String, Field>> fieldMapping = new HashMap<>();
	private boolean removeMissed = false;
	private AssetDao assetDao;
	private FieldDao fieldDao;
	private FieldValueDao fieldValueDao;
	private ClassstructureDao classDao;
	private AttributeDao attrDao;
	private AttributeValueDao attributeValueDao;

	@Inject
	public DAOOut(AssetDao assetDao, FieldValueDao fieldValueDao, ClassstructureDao classDao, AttributeDao attrDao,
				  AttributeValueDao attributeValueDao) {
		this.assetDao = assetDao;
		this.fieldDao = fieldDao;
		this.fieldValueDao = fieldValueDao;
		this.classDao = classDao;
		this.attrDao = attrDao;
		this.attributeValueDao = attributeValueDao;

	}

	@Override
	public void flush(AbstractSystem system) {
		if (isRemoveMissed()) {
			fieldDao.remove(flushFields(system));
			attrDao.remove(flushAttributes(system));
			classDao.remove(flushClassstructures(system));
		} else {
			flushFields(system);
			flushAttributes(system);
			flushClassstructures(system);
		}
		flushAssets(system);
	}

	public boolean isRemoveMissed() {
		return removeMissed;
	}

	public void setRemoveMissed(boolean removeMissed) {
		this.removeMissed = removeMissed;
	}

	private Stream<Long> flushFields(AbstractSystem system) {
		Map<String, Long> existing = fieldDao.mappingWithField("name || useWithString");
		system.getFields().map(system::transform).forEach(field -> {
			String fieldKey = String.format("%s%s", field.getName(), field.getUseWithString());
			if (existing.containsKey(fieldKey)) {
				field.setId(existing.remove(fieldKey));
			}
			if (field.isPersisted()) {
				fieldDao.update(field);
			} else {
				fieldDao.create(field);
			}
		});
		return missedIds(existing);
	}

	private Stream<Long> flushAssets(AbstractSystem system) {
		Map<String, Asset> existing = assetDao.mappingWithField(Asset::getAssetNum);
		Map<String, Long> parents = existing.values().stream()
				.collect(Collectors.toMap(Asset::getAssetNum, Asset::getId));

		system.getAssets().map(system::transform).filter(Objects::nonNull).peek(asset -> {
			if (existing.containsKey(asset.getAssetNum())) {
				Asset exAsset = existing.remove(asset.getAssetNum());
				asset.setId(exAsset.getId());
				asset.setChilds(exAsset.getChilds());
				asset.setParent(exAsset.getParent());
			}
		}).map(this::basicBeanWithClass).filter(Objects::nonNull).peek(asset -> {
			Asset parent = asset.getParent();
			if (parent != null && parent.getAssetNum() != null && !parent.isPersisted()) {
				if (parents.containsKey(parent.getAssetNum())) {
					parent.setId(parents.get(parent.getAssetNum()));
				} else {
					parent.setClassstructure(asset.getClassstructure());
					assetDao.create(parent);
					existing.put(parent.getAssetNum(), parent);
					asset.setParent(parent);
					parents.put(parent.getAssetNum(), parent.getId());
				}
			}
		}).forEach(asset -> {
			if (asset.isPersisted()) {
				assetDao.update(asset);
			} else {
				assetDao.create(asset);
				parents.put(asset.getAssetNum(), asset.getId());
			}
		});
		return missedBeanIds(existing);
	}


	private <T extends BasicBean> T basicBean(T bean) {
		if (bean == null) return null;
		Map<String, Field> beanFields = fieldMapping.getOrDefault(bean.getClass(), new HashMap<>());
		Map<String, FieldValue> existingValues = new HashMap<>();
		if (bean.isPersisted()) {
			existingValues.putAll(fieldValueDao.getValuesByBean(bean));
		}
		bean.setFields(bean.getFields().entrySet().stream().map(valueEntry -> {
			if (beanFields.containsKey(valueEntry.getKey())) {
				valueEntry.getValue().setField(beanFields.get(valueEntry.getKey()));
			} else {
				log.error(String.format("Unable to find field %s for bean %s this value will be ignored",
						valueEntry.getKey(), bean.getClass().getSimpleName()));
				return null;
			}
			FieldValue value = existingValues.getOrDefault(valueEntry.getKey(), valueEntry.getValue());

				try {
						value.setValue(valueEntry.getValue().getValue(fieldValueDao.getEntityManager()));
				} catch (IllegalArgumentException valueProblem) {
					log.error(String.format("Unable to set value of field %s", value.getField().getName()),
							valueProblem);
					return null;
				}
			return value;
		}).filter(Objects::nonNull).collect(Collectors.toMap(FieldValue::getFieldName, Function.identity())));
		return bean;
	}

	private <T extends BasicBeanWithClass> T basicBeanWithClass(T beanWithClass) {
		if (beanWithClass == null) return null;
		final T bean = basicBean(beanWithClass);
		Map<String, AttributeValue> existingValues = new HashMap<>();
		if (bean.isPersisted()) {
			existingValues.putAll(attributeValueDao.getValuesByBean(bean));
		}
		if (bean.getClassstructure() != null) {
			if (classMapping.containsKey(bean.getClassstructure().getClassstructureId())) {
				Classstructure cl = classMapping.get(bean.getClassstructure().getClassstructureId());
				if (!cl.getUseWith().contains(bean.getClass())) {
					log.error(String.format("Classstructure %s can't be applied for this object %s",
							cl.getClassstructureId(), bean.getClass().getSimpleName()));
					return null;
				}
				bean.setClassstructure(cl);
				Map<String, AttributeOnClassstructure> attributes = cl.getAttributeMapping();
				bean.setAttributes(bean.getAttributes().stream().map(attribute -> {
					String attributeName = attribute.getAttributeOnClass().getAttribute().getName();
					AttributeValue value = existingValues.getOrDefault(attributeName, attribute);
					if (!value.isPersisted()) {
						if (attributes.containsKey(attributeName)) {
							value.setAttributeOnClass(attributes.get(attributeName));
						} else {
							log.error(String.format(
									"Unable to find attribute %s for class %s, value will be ignored",
									attributeName, cl.getClassstructureId()));
							return null;
						}
					}
					if (attribute.getType() == null) {
						if (attributes.containsKey(attributeName)) {
							attribute.setAttributeOnClass(attributes.get(attributeName));
						} else {
							log.error(String.format(
									"Unable to find attribute %s for class %s, value will be ignored",
									attributeName, cl.getClassstructureId()));
							return null;
						}
					}
					if (value.isValueSet()) {
						value.setValue(attribute.getValue(attributeValueDao.getEntityManager()));
					}
					return value;
				}).filter(Objects::nonNull).collect(Collectors.toList()));
			} else {
				log.error(String.format("Can't get classstructure %s for object %s, will be ignored",
						bean.getClassstructure().getClassstructureId(), bean.getClass().getSimpleName()));
				return null;
			}
		}
		return bean;
	}

	private Stream<Long> missedIds(Map<?, Long> ids) {
		return ids.values().stream();
	}

	private Stream<Long> missedBeanIds(Map<?, ? extends BasicBean> ids) {
		return ids.values().stream().map(BasicBean::getId);
	}

	private Stream<Long> flushAttributes(AbstractSystem system) {
		Map<String, Long> existing = attrDao.mappingWithField("name");
		system.getAttributes().map(system::transform).forEach(attribute -> {
			if (existing.containsKey(attribute.getName())) {
				attribute.setId(existing.remove(attribute.getName()));
			}
			if (attribute.isPersisted()) {
				attrDao.update(attribute);
			} else {
				attrDao.create(attribute);
			}
		});
		return missedIds(existing);
	}

	private Stream<Long> flushClassstructures(AbstractSystem system) {
		Map<String, Long> existing = classDao.mappingWithField("classstructureId");
		Map<String, Long> parents = new HashMap<>(existing);
		Map<String, Long> attributes = attrDao.mappingWithField("name");

		system.getClassstructures().map(system::transform).forEach(classstructure -> {
			Map<String, Long> existingAttributes = new HashMap<>();
			if (existing.containsKey(classstructure.getClassstructureId())) {
				classstructure.setId(existing.remove(classstructure.getClassstructureId()));
				Classstructure existingClass = classDao.find(classstructure.getId());
				existingAttributes.putAll(existingClass.getAttributesOnClass().stream().collect(Collectors.toMap(
						attr -> attr.getAttribute().getName(),
						AttributeOnClassstructure::getId
				)));
			}
			if (classstructure.getParent() != null) {
				Classstructure parent = classstructure.getParent();
				if (parents.containsKey(parent.getClassstructureId())) {
					classstructure.getParent().setId(parents.get(parent.getClassstructureId()));
				} else {
					classDao.create(parent);
					existing.put(parent.getClassstructureId(), parent.getId());
					parents.put(parent.getClassstructureId(), parent.getId());
					classstructure.setParent(parent);
				}
			}

			classstructure.setAttributesOnClass(classstructure.getAttributesOnClass().stream().map(attrOnClass -> {
				if (attributes.containsKey(attrOnClass.getAttribute().getName())) {
					attrOnClass.getAttribute().setId(attributes.get(attrOnClass.getAttribute().getName()));
					if (existingAttributes.containsKey(attrOnClass.getAttribute().getName())) {
						attrOnClass.setId(existingAttributes.get(attrOnClass.getAttribute().getName()));
					}
					return attrOnClass;
				}
				log.error(String.format(
						"Can't found attribute %s for classstructure %s this relation will be ignored",
						attrOnClass.getAttribute().getName(), attrOnClass.getClassstrcture().getClassstructureId()));
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList()));

			if (classstructure.isPersisted()) {
				classDao.update(classstructure);
			} else {
				classDao.create(classstructure);
				parents.put(classstructure.getClassstructureId(), classstructure.getId());
			}
		});
		return missedIds(existing);
	}

}
