package com.ipc.openeam.desktop.bean.classification;

import com.google.common.base.MoreObjects;
import com.ipc.openeam.desktop.bean.BasicBean;
import com.ipc.openeam.desktop.bean.SystemBean;
import com.ipc.openeam.desktop.bean.attribute.AttributeOnClassstructure;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name="classstructure")
public class Classstructure extends SystemBean  {
	@Id
	@GeneratedValue(generator="classstructure_gen")
	@TableGenerator(name="classstructure_gen", table="mid_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="classstructure", initialValue=1, allocationSize=1)
	private Long id;
	
	@Column(unique=true)
	private String classstructureId;

	private String classificationId;

	private String description;
	
	/* Relations */
	@ManyToOne(optional=true)
	private Classstructure parent;
	
	@ElementCollection
	@CollectionTable(name="class_usewith")
	private Set<Class<? extends BasicBean>> useWith = new HashSet<>();
	
	@OneToMany(mappedBy="parent", cascade= CascadeType.PERSIST, fetch= FetchType.LAZY)
	private List<Classstructure> childs = new ArrayList<>();
	
	@OneToMany(mappedBy="classstructure", cascade= CascadeType.ALL, fetch= FetchType.EAGER)
	@OrderBy("sectionPosition asc, section asc, subSectionPosition asc, subSection asc, attributePosition asc")
	private List<AttributeOnClassstructure> attributesOnClass = new ArrayList<>();

	@Transient
	private Map<String, AttributeOnClassstructure> attributeMapping;
	
	public Classstructure() {
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
				.add("classstructureId", getClassstructureId())
				.add("useWith", getUseWith())
			.toString();
	}

	public String getClassstructureId() {
		return classstructureId;
	}

	public void setClassstructureId(String classstructureId) {
		this.classstructureId = classstructureId;
	}
	
	public void setClassificationId(String classificationId) {
		this.classificationId = classificationId;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Classstructure getParent() {
		return parent;
	}
	
	public void setParent(Classstructure parent) {
		this.parent = parent;
	}
	
	public Set<Class<? extends BasicBean>> getUseWith() {
		return useWith;
	}

	public void setUseWith(Set<Class<? extends BasicBean>> useWith) {
		this.useWith = useWith;
	}
	
	public List<AttributeOnClassstructure> getAttributesOnClass() {
		return attributesOnClass;
	}

	public void setAttributesOnClass(List<AttributeOnClassstructure> attributesOnClass) {
		this.attributesOnClass = attributesOnClass;
	}

	public Map<String, AttributeOnClassstructure> getAttributeMapping() {
		return getAttributesOnClass().stream().collect(Collectors.toMap(
				attr -> attr.getAttribute().getName(),
				attr -> attr
		));
	}
}
