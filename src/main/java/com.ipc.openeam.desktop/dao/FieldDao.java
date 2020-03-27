package com.ipc.openeam.desktop.dao;


import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.field.Field;

public class FieldDao extends AbstractDao<Field> {
	@Inject
	public FieldDao(Provider<EntityManager> emp) {
		super(Field.class, emp);
	}

}
