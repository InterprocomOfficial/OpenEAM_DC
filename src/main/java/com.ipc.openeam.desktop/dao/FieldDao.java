package com.ipc.openeam.desktop.dao;


import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.field.Field;
import com.ipc.openeam.desktop.bean.BasicBean;


import java.util.Arrays;
import java.util.List;

public class FieldDao extends AbstractDao<Field> {
	@Inject
	public FieldDao(Provider<EntityManager> emp) {
		super(Field.class, emp);
	}

	@SuppressWarnings("unchecked")
	public List<Field> findByUseWith(Class<? extends BasicBean> useWith) {
		return (List<Field>) getEntityManager().createQuery("select f from Field f where f.useWith = :useWith")
				.setParameter("useWith", useWith).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Field> findByUseWith(Class<? extends BasicBean>... useWith) {
		return (List<Field>) getEntityManager().createQuery("select f from Field f where f.useWith in :useWith")
				.setParameter("useWith", Arrays.asList(useWith)).getResultList();
	}

}
