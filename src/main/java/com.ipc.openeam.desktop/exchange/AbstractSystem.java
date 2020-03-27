package com.ipc.openeam.desktop.exchange;

import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.field.Field;

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


	public Field transform(Field field) {
		return field;
	}

	public Asset transform(Asset asset) {
		return asset;
	}

}
