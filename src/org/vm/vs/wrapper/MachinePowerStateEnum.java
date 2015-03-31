package org.vm.vs.wrapper;

import java.util.HashMap;

import com.vmware.vim25.VirtualMachinePowerState;

/**
 * 
 * @author fender
 *
 */
public enum MachinePowerStateEnum {
	
	RUNNING(VirtualMachinePowerState.poweredOn), 
	STOPPED(VirtualMachinePowerState.poweredOff), 
	SUSPENDED(VirtualMachinePowerState.suspended);
	
	private VirtualMachinePowerState state;
	private static HashMap<VirtualMachinePowerState, MachinePowerStateEnum> lookup = 
			new HashMap<VirtualMachinePowerState, MachinePowerStateEnum>();
	
	static {
		for (MachinePowerStateEnum s : MachinePowerStateEnum.values()) {
			lookup.put(s.getPowerState(), s);
		}
	}
	
	private MachinePowerStateEnum(VirtualMachinePowerState state) {
		this.state = state;
	}
	
	public MachinePowerStateEnum getState(VirtualMachinePowerState state) {
		return lookup.get(state);
	}
	
	private VirtualMachinePowerState getPowerState() {
		return state;
	}
}
