package com.ipc.openeam.desktop.bean.field;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.ipc.openeam.desktop.bean.BasicBean;
import org.eclipse.persistence.annotations.Index;

import com.ipc.openeam.desktop.bean.BeanProperty;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.SystemBean;

import com.google.common.base.MoreObjects;

@Entity
@Table(name="field")
@Index(name="FIELD_NDX", columnNames={"name", "useWith"}, unique=true)
public class Field extends SystemBean implements BeanProperty {
	@Id
	@GeneratedValue(generator="field_gen")
	@TableGenerator(name="field_gen", table="eam_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="field", initialValue=1, allocationSize=1)
	private Long id;
	
	@Column(nullable=false)
	private String name;	
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private BeanPropertyType type;
	private Integer length;
	private String defaultValue;
	private String description;

	@Column(nullable=false)
	private Class<? extends BasicBean> useWith;

	@Column(nullable=false)
	private String useWithString;

	/* Relations */
	@ManyToOne(optional=true)
	
	@Column(nullable=false)

	@OneToMany(mappedBy="field", fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	private List<FieldValue> values = new ArrayList<>();
	
	public Field() {
		super();
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", getId())
				.add("name", getName())
				.add("type", getType())
			.toString();
	}
	public void setUseWith(Class<? extends BasicBean> useWith) {
		this.useWith = useWith;
		this.useWithString = useWith.getSimpleName();
	}

	public String getUseWithString() {
		return useWithString;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Class<? extends BasicBean> getUseWith() {
		return useWith;
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public BeanPropertyType getType() {
		return type;
	}
	
	public void setType(BeanPropertyType type) {
		this.type = type;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

}
