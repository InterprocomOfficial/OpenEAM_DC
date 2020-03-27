package com.ipc.openeam.desktop.dao;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;


import com.google.inject.persist.Transactional;
import com.ipc.openeam.desktop.bean.BasicBean;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.persistence.config.QueryHints;


public abstract class AbstractDao<T> {
	protected final Logger log = Logger.getLogger(getClass());
	protected Class<T> entityClass;
	private final Provider<EntityManager> emp;
	
	@Inject
	public AbstractDao(Class<T> entityClass, Provider<EntityManager> emp) {
		this.entityClass = entityClass;
		this.emp = emp;
		this.emp.get().clear();
	}

	public EntityManager getEntityManager() {
		return this.emp.get();
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder()
				.createQuery(entityClass);
		cq.select(cq.from(entityClass));

		Query query = getEntityManager().createQuery(cq);
		query.setHint(QueryHints.REFRESH, true);

		return query.getResultList();
	}

	public <U> Map<U, T> mappingWithField(Function<T, U> fn) {
		return findAll().stream().collect(Collectors.toMap(fn, v -> v));
	}

	public <U> Map<U, Long> mappingWithField(String fieldName) {
		Query query = getEntityManager().createNativeQuery(
				String.format("SELECT %s, id FROM %s", fieldName, getTableName()));
		return ((List<Object[]>) query.getResultList()).stream().collect(Collectors.toMap(
				row -> (U) row[0],
				row -> Long.valueOf((Integer) row[1])
		));
	}

	public String getTableName() {
		Metamodel meta = getEntityManager().getMetamodel();
		EntityType<T> entityType = meta.entity(entityClass);
		Table t = entityClass.getAnnotation(Table.class);
		return (t == null) ? entityType.getName().toUpperCase() : t.name();
	}

	public T find(Object id) {
		return getEntityManager().find(entityClass, id);
	}
	@Transactional
	public void remove(T entity) {
		getEntityManager().remove(getEntityManager().merge(entity));
	}

	@Transactional
	public void remove(Long entityId) {
		T entity = find(entityId);

		if (entity != null) {
			remove(entity);
		}
	}

	public void create(T entity) {
		getEntityManager().persist(entity);
	}

	public void remove(Stream<Long> ids) {
		ids.forEach(this::remove);
	}

	@Transactional
	public void update(T entity) {
		if (entity instanceof BasicBean) {
			((BasicBean) entity).getFields().values().stream().filter(fieldValue -> !fieldValue.isPersisted())
					.forEach(fieldValue -> getEntityManager().persist(fieldValue));
		}

		getEntityManager().merge(entity);
	}

}
