package com.ipc.openeam.desktop.bean.field;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Index;


import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.BeanPropertyValue;


@Entity
@Table(name="field_value")
@Index(name="FIELD_VALUE_INDX", columnNames={"bean_id", "field_id"}, unique=true)
public class FieldValue extends BeanPropertyValue {
	@JoinColumn(name="field_id", nullable=false)
	@ManyToOne(fetch=FetchType.EAGER)
	private Field field;
	
	private String fieldName;
	
	/* Relations */
	@JoinColumn(name="bean_id", nullable=false)
	@ManyToOne(fetch=FetchType.LAZY)
	private BasicBean bean;
	
	public FieldValue() {
		super();
	}
	
	@Override
	public BeanPropertyType getType() {
		return getField().getType();
	}


	public Field getField() {
		return field;
	}
	
	public void setField(Field field) {
		this.field = field;
		this.fieldName = field.getName();
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public BasicBean getBean() {
		return bean;
	}

	public void setBean(BasicBean bean) {
		this.bean = bean;
	}
}
