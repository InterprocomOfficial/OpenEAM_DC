package com.ipc.openeam.desktop.util;

import com.ipc.openeam.desktop.bean.SystemBean;
import com.ipc.openeam.desktop.bean.asset.Asset;
import com.ipc.openeam.desktop.bean.classification.Classstructure;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExternalSysObjectMapping {
	private static Map<String, Class<? extends SystemBean>> mapping = new HashMap<>();
	private static Map<Class<? extends SystemBean>, String> reverseMapping;
	
	static {
		// Объекты
		mapping.put("ASSET", Asset.class);
		mapping.put("CLASSSTRUCTURE", Classstructure.class);

		// Формирование обратного маппинга
		reverseMapping = mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
	}
	
	public static Class<? extends SystemBean> get(String name) throws ClassNotFoundException {
		Class<? extends SystemBean> cls = mapping.get(name);
		if (cls == null) {
			throw new ClassNotFoundException(String.format("Can't find class in object mapping for MBO %s", name));
		}
		return cls;
	}

	public static String get(Class<? extends SystemBean> name) {
		return reverseMapping.get(name);
	}
}
