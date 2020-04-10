package com.ipc.openeam.desktop.exchange;

import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.attribute.Attribute;
import com.ipc.openeam.desktop.bean.classification.Classstructure;

import java.util.stream.Stream;

public abstract class AbstractSystem implements BeanAccessible {
	private AbstractIn in;

	public void setIn(AbstractIn in) {
		this.in = in;
	}

	public AbstractIn getIn() {
		return in;
	}

	@Override public Stream<Field> getFields() {
		return getIn().getFields();
	}
	@Override public Stream<Asset> getAssets() {
		return getIn().getAssets();
	}
	@Override public Stream<Attribute> getAttributes() {
		return getIn().getAttributes();
	}
	@Override public Stream<Classstructure> getClassstructures() {
		return getIn().getClassstructures();
	}


	public Field transform(Field field) {
		return field;
	}

	public Asset transform(Asset asset) {
		return asset;
	}

	public Attribute transform(Attribute attribute) {
		return attribute;
	}

	public Classstructure transform(Classstructure classstructure) {
		return classstructure;
	}
}
