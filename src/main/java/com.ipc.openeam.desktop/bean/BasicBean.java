package com.ipc.openeam.desktop.bean;

import com.ipc.openeam.desktop.bean.field.FieldValue;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class BasicBean extends SystemBean {
	@Id
	@GeneratedValue(generator="bean_gen")
	@TableGenerator(name="bean_gen", table="eam_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="bean", initialValue=1, allocationSize=1)
	private Long id;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)

	@OneToMany(mappedBy="bean", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@MapKey(name="fieldName")
	private Map<String, FieldValue> fields = new HashMap<>();

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, FieldValue> getFields() {
		return fields;
	}

	public void setFields(Map<String, FieldValue> fields) {
		this.fields = fields;
	}


}
