package com.ipc.openeam.desktop.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.attribute.Attribute;

public class AttributeDao extends AbstractDao<Attribute> {
	@Inject
	public AttributeDao(Provider<EntityManager> emp) {
		super(Attribute.class, emp);
	}

}
