package com.ipc.openeam.desktop.bean.attribute;

import com.google.common.base.MoreObjects;
import com.ipc.openeam.desktop.bean.BasicBeanWithClass;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.BeanPropertyValue;
import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;

@Entity
@Table(name="attribute_value")
@Index(name="ATTR_VALUE_NDX", columnNames={"bean_id", "attribute_on_class_id"}, unique=true)
public class AttributeValue extends BeanPropertyValue {
	/* Relations */
	@JoinColumn(name="bean_id", nullable=false)
	@ManyToOne(fetch= FetchType.LAZY)
	private BasicBeanWithClass bean;
	
	@JoinColumn(name="attribute_on_class_id", nullable=false)
	@ManyToOne(fetch= FetchType.EAGER)
	private AttributeOnClassstructure attributeOnClass;
	
	public AttributeValue() {
		super();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", getId())
				.add("attributeOnClass", getAttributeOnClass())
			.toString();
	}

	@Override
	public BeanPropertyType getType() {
		return getAttributeOnClass().getAttribute().getType();
	}

	public BasicBeanWithClass getBean() {
		return bean;
	}

	public void setBean(BasicBeanWithClass bean) {
		this.bean = bean;
	}
	
	public AttributeOnClassstructure getAttributeOnClass() {
		return attributeOnClass;
	}
	
	public void setAttributeOnClass(AttributeOnClassstructure attributeOnClass) {
		this.attributeOnClass = attributeOnClass;
	}
}
