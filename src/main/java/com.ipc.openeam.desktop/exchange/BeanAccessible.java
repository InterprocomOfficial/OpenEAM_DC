package com.ipc.openeam.desktop.exchange;

import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.attribute.Attribute;
import com.ipc.openeam.desktop.bean.classification.Classstructure;

import java.util.stream.Stream;

public interface BeanAccessible {
	Stream<Field> getFields();
	Stream<Asset> getAssets();
	Stream<Attribute> getAttributes();
	Stream<Classstructure> getClassstructures();
}
