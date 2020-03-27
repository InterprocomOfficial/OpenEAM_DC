package com.ipc.openeam.desktop.exchange.point.out;

import com.google.inject.Inject;
import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.field.FieldValue;
import com.ipc.openeam.desktop.dao.*;
import com.ipc.openeam.desktop.exchange.AbstractOut;
import com.ipc.openeam.desktop.exchange.AbstractSystem;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DAOOut extends AbstractOut {
	private static Logger log = Logger.getLogger(DAOOut.class);
	private Map<Class<? extends BasicBean>, Map<String, Field>> fieldMapping = new HashMap<>();
	private boolean removeMissed = false;
	private AssetDao assetDao;
	private FieldDao fieldDao;
	private FieldValueDao fieldValueDao;

	@Inject
	public DAOOut(AssetDao assetDao, FieldValueDao fieldValueDao) {
		this.assetDao = assetDao;
		this.fieldDao = fieldDao;
		this.fieldValueDao = fieldValueDao;
	}

	@Override
	public void flush(AbstractSystem system) {
		if (isRemoveMissed()) {
			fieldDao.remove(flushFields(system));
		} else {
			flushFields(system);
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
			}
		}).map(this::basicBean ).filter(Objects::nonNull).peek(asset -> {
			Asset parent = asset.getParent();
			if (parent != null && parent.getAssetNum() != null && !parent.isPersisted()) {
				if (parents.containsKey(parent.getAssetNum())) {
					parent.setId(parents.get(parent.getAssetNum()));
				} else {
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

	private Stream<Long> missedIds(Map<?, Long> ids) {
		return ids.values().stream();
	}

	private Stream<Long> missedBeanIds(Map<?, ? extends BasicBean> ids) {
		return ids.values().stream().map(BasicBean::getId);
	}
}
