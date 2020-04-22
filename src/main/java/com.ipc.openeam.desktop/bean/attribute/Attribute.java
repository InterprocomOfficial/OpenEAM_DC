package com.ipc.openeam.desktop.bean.attribute;

import com.google.common.base.MoreObjects;
import com.ipc.openeam.desktop.bean.BeanProperty;
import com.ipc.openeam.desktop.bean.BeanPropertyType;
import com.ipc.openeam.desktop.bean.SystemBean;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="attribute")
public class Attribute extends SystemBean implements BeanProperty {
	@Id
	@GeneratedValue(generator="attribute_gen")
	@TableGenerator(name="attribute_gen", table="mid_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="attribute", initialValue=1, allocationSize=1)
	private Long id;
	
	@Column(unique=true, nullable=false)
	private String name;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private BeanPropertyType type;
	
	private String description;

	/* Relations */
	@OneToMany(mappedBy="attribute", fetch= FetchType.LAZY, cascade={CascadeType.REMOVE, CascadeType.REFRESH})
	private List<AttributeOnClassstructure> attributesOnClass = new ArrayList<>();
	
	public Attribute() {
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
				.add("attributesOnClass", getAttributesOnClass().size())
			.toString();
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

	public BeanPropertyType getType() {
		return type;
	}
	
	public void setType(BeanPropertyType type) {
		this.type = type;
	}
	
	public List<AttributeOnClassstructure> getAttributesOnClass() {
		return attributesOnClass;
	}

	public String getDescription() {
		return description;
	}

}
