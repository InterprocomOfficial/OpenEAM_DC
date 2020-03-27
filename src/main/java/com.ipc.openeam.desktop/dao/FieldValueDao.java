package com.ipc.openeam.desktop.dao;

import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.field.FieldValue;

public class FieldValueDao extends AbstractDao<FieldValue> {
	@Inject
	public FieldValueDao(Provider<EntityManager> emp) {
		super(FieldValue.class, emp);
	}

	@SuppressWarnings("unchecked")
	public Map<String, FieldValue> getValuesByBean(BasicBean bean) {
		Query query = getEntityManager().createQuery(
				"select fv from FieldValue fv where fv.bean.id = :id", FieldValue.class);
		query.setParameter("id", bean.getId());
		return ((List<FieldValue>) query.getResultList()).stream().collect(Collectors.toMap(
				FieldValue::getFieldName,
				v -> v
		));
	}

}
