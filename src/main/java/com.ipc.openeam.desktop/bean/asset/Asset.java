package com.ipc.openeam.desktop.bean.asset;

import java.util.*;

import javax.persistence.*;

import com.ipc.openeam.desktop.bean.BasicBeanWithClass;


@Entity
@Table(name="asset")
public class Asset extends BasicBeanWithClass{
	@Column(unique=true, nullable=false)
	private String assetNum;

	@ManyToOne
	private Asset parent;

	@OrderBy("description asc")
	@OneToMany(mappedBy="parent", cascade={CascadeType.REFRESH, CascadeType.REMOVE}, orphanRemoval=true)
	private List<Asset> childs = new ArrayList<Asset>();
	
	public Asset() {
		super();
	}


	public void setParent(Asset parent) {
		this.parent = parent;
	}

	public Asset getParent() {
		return this.parent;
	}

	public void setChilds(List<Asset> childs) {
		this.childs = childs;
	}

	public List<Asset> getChilds() {
		return this.childs;
	}

	public void setAssetNum(String assetNum) {
		this.assetNum = assetNum;
	}

	public String getAssetNum() {
		return assetNum;
	}


}
