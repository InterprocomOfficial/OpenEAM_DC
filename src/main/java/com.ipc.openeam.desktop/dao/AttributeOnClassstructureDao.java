package com.ipc.openeam.desktop.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.attribute.AttributeOnClassstructure;

public class AttributeOnClassstructureDao extends AbstractDao<AttributeOnClassstructure> {
	@Inject
	public AttributeOnClassstructureDao(Provider<EntityManager> emp) {
		super(AttributeOnClassstructure.class, emp);
	}
}
