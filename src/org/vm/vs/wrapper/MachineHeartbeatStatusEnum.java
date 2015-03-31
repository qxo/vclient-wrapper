package org.vm.vs.wrapper;

import java.util.HashMap;

import com.vmware.vim25.ManagedEntityStatus;

/**
 * 
 * @author fender
 *
 */
public enum MachineHeartbeatStatusEnum {

	UNKNOWN(ManagedEntityStatus.gray),
	OK(ManagedEntityStatus.green),
	CRITICAL(ManagedEntityStatus.red),
	WARNING(ManagedEntityStatus.yellow);

	private ManagedEntityStatus status;
	private static HashMap<ManagedEntityStatus, MachineHeartbeatStatusEnum> lookup = 
			new HashMap<ManagedEntityStatus, MachineHeartbeatStatusEnum>();
	
	static {
		for (MachineHeartbeatStatusEnum s : MachineHeartbeatStatusEnum.values()) {
			lookup.put(s.getStatus(), s);
		}
	}
	
	private MachineHeartbeatStatusEnum(ManagedEntityStatus status) {
		this.status = status;
	}
	
	private ManagedEntityStatus getStatus() {
		return status;
	}
	
	public MachineHeartbeatStatusEnum getVmStatus(ManagedEntityStatus status) {
		return lookup.get(status);
	}
}
