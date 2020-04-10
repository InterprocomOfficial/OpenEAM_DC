package com.ipc.openeam.desktop.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.classification.Classstructure;

public class ClassstructureDao extends AbstractDao<Classstructure> {
	@Inject
	public ClassstructureDao(Provider<EntityManager> emp) {
		super(Classstructure.class, emp);
	}
}
