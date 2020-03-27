package com.ipc.openeam.desktop.exchange.point.in;

import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.field.FieldValue;
import com.ipc.openeam.desktop.exchange.AbstractIn;
import com.ipc.openeam.desktop.util.ExternalSysObjectMapping;
import com.ipc.openeam.desktop.util.Utils;
import org.apache.log4j.Logger;
import org.apache.commons.io.CopyUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

}
