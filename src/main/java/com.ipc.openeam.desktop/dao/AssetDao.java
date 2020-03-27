package com.ipc.openeam.desktop.dao;

import javax.persistence.EntityManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ipc.openeam.desktop.bean.asset.Asset;

public class AssetDao extends AbstractDao<Asset> {
	@Inject
	public AssetDao(Provider<EntityManager> emp) {
		super(Asset.class, emp);
	}

}
