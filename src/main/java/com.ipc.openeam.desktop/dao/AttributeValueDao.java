package com.ipc.openeam.desktop.dao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.BasicBeanWithClass;
import com.ipc.openeam.desktop.bean.attribute.AttributeValue;

public class AttributeValueDao extends AbstractDao<AttributeValue> {
	@Inject
	public AttributeValueDao(Provider<EntityManager> emp) {
		super(AttributeValue.class, emp);
	}

	@SuppressWarnings("unchecked")
	public Map<String, AttributeValue> getValuesByBean(BasicBeanWithClass bean) {
		Query query = getEntityManager().createQuery(
				"select av from AttributeValue av where av.bean.id = :id", AttributeValue.class);
		query.setParameter("id", bean.getId());
		return ((List<AttributeValue>) query.getResultList()).stream().collect(Collectors.toMap(
				v -> v.getAttributeOnClass().getAttribute().getName(),
				v -> v
		));
	}
}
