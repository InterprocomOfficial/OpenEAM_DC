package com.ipc.openeam.desktop.bean;

public abstract class SystemBean {

	public SystemBean() {}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj.getClass().isAssignableFrom(this.getClass())) {
			if (getId() != null && ((SystemBean) obj).getId() != null) {
				return getId().equals(((SystemBean) obj).getId());
				}
			}
		return false;
	}

	public boolean isPersisted() {
		return getId() != null;
	}
	
	public abstract Long getId();
	public abstract void setId(Long id);
}
