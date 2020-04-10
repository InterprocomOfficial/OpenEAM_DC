package com.ipc.openeam.desktop.exchange.point.in;

import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.BasicBeanWithClass;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.field.FieldValue;
import com.ipc.openeam.desktop.exchange.AbstractIn;
import com.ipc.openeam.desktop.util.ExternalSysObjectMapping;
import com.ipc.openeam.desktop.util.Utils;
import com.ipc.openeam.desktop.bean.attribute.Attribute;
import com.ipc.openeam.desktop.bean.attribute.AttributeOnClassstructure;
import com.ipc.openeam.desktop.bean.classification.Classstructure;
import com.ipc.openeam.desktop.bean.attribute.AttributeValue;


import org.apache.log4j.Logger;
import org.apache.commons.io.CopyUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XMLIn extends AbstractIn {
	private static Logger log = Logger.getLogger(XMLIn.class);
	private ByteArrayOutputStream inData = new ByteArrayOutputStream();
	private org.dom4j.Document root;

	public XMLIn(InputStream input) throws IOException, DocumentException {
		CopyUtils.copy(input, inData);
		this.root = new SAXReader().read(getInput());
	}

	@Override public Stream<Field> getFields() {
		return Utils.getNodeListByPath(getRoot(), "//openeam:openEAMPackage/openeam:metaData/openeam:fieldSet/openeam:field")
				.stream().map(Element.class::cast).map(fieldEl -> {
					Field field = new Field();
					field.setName(fieldEl.elementText("name"));
					field.setType(BeanPropertyType.valueOf(fieldEl.elementText("type")));
					field.setDescription(fieldEl.elementText("description"));

					if (fieldEl.elementText("length") != null) {
						field.setLength(Integer.parseInt(fieldEl.elementText("length")));
					}

					try {
						field.setUseWith(
								(Class<? extends BasicBean>) ExternalSysObjectMapping.get(
										fieldEl.elementText("useWith")));
					} catch (ClassNotFoundException e) {
						log.error(String.format("On get BasicBean class %s for field %s",
								fieldEl.elementText("useWith"), field.getName()));
					}
					String defaultValue = fieldEl.elementText("defaultValue");
					if (defaultValue != null && !defaultValue.isEmpty() && !defaultValue.equals("NONE")) {
						field.setDefaultValue(defaultValue);
					}

					return field;
				}).filter(field -> field.getUseWith() != null);
	}

	public InputStream getInput() {
		return new ByteArrayInputStream(inData.toByteArray());
	}

	public org.dom4j.Document getRoot() {
		return root;
	}

	@Override public Stream<Asset> getAssets() {
		return Utils.getNodeListByPath(getRoot(),
				"//openeam:openEAMPackage/openeam:objectSets/openeam:assetSet/openeam:asset").stream()
				.map(Element.class::cast).map(assetEl -> {
					Asset asset = new Asset();
					asset.setAssetNum(assetEl.elementText("assetNum"));

					String parentId = assetEl.elementText("parent");
					if (parentId != null && !parentId.isEmpty()) {
						Asset parentAsset = new Asset();
						parentAsset.setAssetNum(parentId);
						asset.setParent(parentAsset);
					}

					return buildBasicBeanFields(asset, assetEl);
		});
	}

	@Override public Stream<Attribute> getAttributes() {
		return Utils.getNodeListByPath(getRoot(), "//openeam:openEAMPackage/openeam:metaData/openeam:attributeSet/openeam:attribute")
				.stream().map(Element.class::cast).map(attributeEl -> {
					Attribute attribute = new Attribute();
					attribute.setName(attributeEl.elementText("name"));
					attribute.setType(BeanPropertyType.valueOf(attributeEl.elementText("type")));
					attribute.setDescription(attributeEl.elementText("description"));
					return attribute;
				});
	}

	@SuppressWarnings("unchecked")
	@Override public Stream<Classstructure> getClassstructures() {
		return Utils.getNodeListByPath(getRoot(),
				"//openeam:openEAMPackage/openeam:metaData/openeam:classstructureSet/openeam:classstructure").stream()
				.map(Element.class::cast).map(classEl -> {
					Classstructure clazz = new Classstructure();
					clazz.setClassstructureId(classEl.elementText("classstructureId"));
					clazz.setClassificationId(classEl.elementText("classificationId"));
					clazz.setDescription(classEl.elementText("description"));

					// Статус классификации
					String classStatusName = classEl.elementText("status");

					// Родительская классификация
					String parentId = classEl.elementText("parent");
					if (parentId != null && !parentId.isEmpty()) {
						Classstructure parent = new Classstructure();
						parent.setClassstructureId(parentId);
						clazz.setParent(parent);
					}

					// Привязка классификации к объектам
					clazz.setUseWith(Utils.getNodeListByPath(classEl, "openeam:useWith/openeam:object").stream()
							.map(Element.class::cast).map(obj -> {
								try {
									return (Class<? extends BasicBean>) ExternalSysObjectMapping.get(obj.getText());
								} catch (ClassNotFoundException e) {
									return null;
								}
							}).filter(Objects::nonNull).collect(Collectors.toSet()));

					// Атрибуты классификации
					clazz.setAttributesOnClass(
							Utils.getNodeListByPath(classEl, "openeam:attributes/openeam:attribute").stream()
									.map(Element.class::cast).map(attributeEl -> {
								AttributeOnClassstructure attrOnClass = new AttributeOnClassstructure();
								attrOnClass.setClassstructure(clazz);
								attrOnClass.setSection(attributeEl.elementText("section"));
								attrOnClass.setSubSection(attributeEl.elementText("subSection"));

								/* Последовательность расположения компонентов интерфейса задается этими параметрами
								Если в исходном сообщении не передана такая информация проставляем позиции равные 0 */
								if (attributeEl.elementText("attributePosition") != null) {
									attrOnClass.setAttributePosition(
											Integer.parseInt(attributeEl.elementText("attributePosition")));
								}
								if (attributeEl.elementText("sectionPosition") != null) {
									attrOnClass.setSectionPosition(Integer.parseInt(
											attributeEl.elementText("sectionPosition")));
								}
								if (attributeEl.elementText("subSectionPosition") != null) {
									attrOnClass.setSubSectionPosition(Integer.parseInt(
											attributeEl.elementText("subSectionPosition")));
								}

								// Связь с атрибутом
								Attribute attribute = new Attribute();
								attribute.setName(attributeEl.elementText("name"));
								attrOnClass.setAttribute(attribute);

								return attrOnClass;
							}).collect(Collectors.toList()));
					return clazz;
				});
	}


	protected <T extends BasicBean> T buildBasicBean(T bean, Element beanEl) {

		return buildBasicBeanFields(bean, beanEl);
	}

	protected <T extends BasicBeanWithClass> T buildBasicBeanWithClass(T bean, Element beanEl) {
		bean = buildBasicBean(bean, beanEl);
		Classstructure classstructure = new Classstructure();
		classstructure.setClassstructureId(beanEl.elementText("classstructure"));
		bean.setClassstructure(classstructure);

		return buildBasicBeanWithClassAttributes(bean, beanEl);
	}

	protected <T extends BasicBean> T buildBasicBeanFields(T bean, Element beanEl) {
		bean.setFields(Utils.getNodeListByPath(beanEl, "openeam:fields/openeam:field").stream()
				.map(Element.class::cast).map(fieldEl -> {
					Field field = new Field();
					field.setName(fieldEl.elementText("name"));

					FieldValue fieldValue = new FieldValue();
					fieldValue.setBean(bean);
					fieldValue.setField(field);

					return fieldValue;
				}).collect(Collectors.toMap(v -> v.getField().getName(), v -> v)));
		return bean;
	}

	protected <T extends BasicBeanWithClass> T buildBasicBeanWithClassAttributes(T bean, Element beanEl) {
		bean.setAttributes(Utils.getNodeListByPath(beanEl, "openeam:attributes/openeam:attribute")
				.stream().map(Element.class::cast).map(attributeEl -> {
					Attribute attribute = new Attribute();
					attribute.setName(attributeEl.elementText("name"));

					AttributeOnClassstructure attrOnClass = new AttributeOnClassstructure();
					attrOnClass.setAttribute(attribute);
					attrOnClass.setClassstructure(bean.getClassstructure());

					AttributeValue value = new AttributeValue();
					value.setBean(bean);
					value.setAttributeOnClass(attrOnClass);
					return value;
				}).collect(Collectors.toList()));
		return bean;
	}
}
