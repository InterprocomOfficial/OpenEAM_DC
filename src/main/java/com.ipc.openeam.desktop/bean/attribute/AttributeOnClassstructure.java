package com.ipc.openeam.desktop.bean.attribute;

import com.google.common.base.MoreObjects;
import com.ipc.openeam.desktop.bean.SystemBean;
import com.ipc.openeam.desktop.bean.classification.Classstructure;
import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="attribute_on_classstructure")
@Index(name="ATTR_ON_CLASS_NDX", columnNames={"attribute_id", "classstructure_id", "section", "subsection"}, unique=true)
public class AttributeOnClassstructure extends SystemBean {
	@Id
	@GeneratedValue(generator="attr_on_class_gen")
	@TableGenerator(name="attr_on_class_gen", table="mid_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="attribute_on_classstructure", initialValue=1, allocationSize=1)
	private Long id;
	
	@Column(name="section")
	private String section;
	
	@Column(name="subsection")
	private String subSection;
	
	private Integer attributePosition = 0;
	private Integer sectionPosition = 0;
	private Integer subSectionPosition = 0;

	/* Relations */
	@JoinColumn(name="attribute_id", nullable=false)
	@ManyToOne(fetch= FetchType.EAGER)
	private Attribute attribute;
	
	@JoinColumn(name="classstructure_id", nullable=false)
	@ManyToOne(fetch= FetchType.EAGER)
	private Classstructure classstructure;
	
	@OneToMany(mappedBy="attributeOnClass", cascade= CascadeType.REMOVE, fetch= FetchType.LAZY)
	private List<AttributeValue> values = new ArrayList<>();
	
	public AttributeOnClassstructure() {
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
				.add("attribute", getAttribute())
				.add("classstructure", getClassstrcture())
			.toString();
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Classstructure getClassstrcture() {
		return classstructure;
	}
	
	public void setClassstructure(Classstructure classstructure) {
		this.classstructure = classstructure;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public void setSubSection(String subSection) {
		this.subSection = subSection;
	}

	public void setAttributePosition(Integer attributePosition) {
		this.attributePosition = attributePosition;
	}

	public void setSectionPosition(Integer sectionPosition) {
		this.sectionPosition = sectionPosition;
	}

	public void setSubSectionPosition(Integer subSectionPosition) {
		this.subSectionPosition = subSectionPosition;
	}

}
