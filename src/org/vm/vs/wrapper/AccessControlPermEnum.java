package org.vm.vs.wrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author fender
 *
 */
public enum AccessControlPermEnum {
	
	ROOT(2),	// can read vm info, start/stop/restart virtual machines and have remote access control
	ADMIN(1),	// can read vm info, start/stop/restart virtual machines
	USER(0)		// can read vm info
	;
	
	private int id;
	private static Map<Integer, AccessControlPermEnum> lookup = new HashMap<Integer, AccessControlPermEnum>();
	
	static {
		for (AccessControlPermEnum a : AccessControlPermEnum.values()) {
			lookup.put(a.getId(), a);
		}
	}
	
	private AccessControlPermEnum(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public AccessControlPermEnum getAccessControl(int id) {
		AccessControlPermEnum control = lookup.get(Integer.valueOf(id));
		return control == null ? USER : control;
	}
		
}
