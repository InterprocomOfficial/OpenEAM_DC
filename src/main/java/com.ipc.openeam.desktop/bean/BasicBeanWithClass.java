package com.ipc.openeam.desktop.bean;

import com.google.common.base.MoreObjects;
import com.ipc.openeam.desktop.bean.attribute.AttributeValue;
import com.ipc.openeam.desktop.bean.classification.Classstructure;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class BasicBeanWithClass extends BasicBean {
	@JoinColumn(nullable=false)
	@ManyToOne(fetch= FetchType.EAGER)
	private Classstructure classstructure;
	
	@OneToMany(mappedBy="bean", fetch= FetchType.EAGER, cascade= CascadeType.ALL)
	private List<AttributeValue> attributes = new ArrayList<>();

	@Transient
	private Map<String, AttributeValue> attributeMapping;
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", getId())
				.add("fields", getFields().size())
				.add("attributes", getAttributes().size())
			.toString();
	}
	
	public Classstructure getClassstructure() {
		return classstructure;
	}

	public void setClassstructure(Classstructure classstructure) {
		this.classstructure = classstructure;
	}
	
	public List<AttributeValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeValue> attributes) {
		this.attributes = attributes;
	}

	public AttributeValue findAttributeByName(String name, boolean reload) {
		if (attributeMapping == null || reload) {
			attributeMapping = new HashMap<>();
		}
		if (!attributeMapping.containsKey(name)) {
			getAttributes().stream().filter(attr -> attr.getAttributeOnClass().getAttribute().getName().equals(name))
					.findAny().ifPresent(attr -> {
				attributeMapping.put(name, attr);
			});
		}
		return attributeMapping.get(name);
	}

	public AttributeValue findAttributeByName(String name) {
		return findAttributeByName(name, false);
	}

	public void addAttribute(AttributeValue attrValue) {
		if (!getAttributes().contains(attrValue)) {
			getAttributes().add(attrValue);
		}
	}
}
