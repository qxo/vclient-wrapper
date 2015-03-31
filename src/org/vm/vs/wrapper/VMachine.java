package org.vm.vs.wrapper;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * @author fender
 *
 */
public class VMachine implements Machine {

	private static Logger log = Logger.getLogger(VMachine.class);
	
	private VirtualMachine vm;
	
	private AccessControlPermEnum permission = AccessControlPermEnum.USER;
	
	
	public VMachine(VirtualMachine vm) {
		this.vm = vm;
		
		// Sets the access control to the virtual machine.
		try {
			String mask = VUtil.getCustomAttributeValue(vm, VMachineInfo.VM_CFIELD_CONTROL_MASK_NAME);
			
			if (mask != null) {
				int id = Integer.valueOf(mask).intValue();
				permission = permission.getAccessControl(id);
			}
			
		} catch (Exception e) {
			log.info("The VM's control mask could not be retrieved", e);			
		}		

	}
	
	@Override
	public void start() 
			throws VmConfigFault, TaskInProgress, FileFault, InvalidState, InsufficientResourcesFault, RuntimeFault, 
			RemoteException, InterruptedException, UserPermException {
		
		switch (permission) {
		case ROOT:
		case ADMIN:
			Task task = vm.powerOnVM_Task(null);
			if (task.waitForTask() == Task.SUCCESS) {
				log.debug("VM on!");
			}			
			break;
			
		default:
			throw new UserPermException("The user doesn't have enough permissions to execute this action.");
		}
	}

	@Override
	public void stop() 
			throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException, 
			UserPermException {
		
		switch (permission) {
		case ROOT:
		case ADMIN:		
			Task task = vm.powerOffVM_Task();
			if (task.waitForTask() == Task.SUCCESS) {
				log.debug("VM reset!");
			}
			break;
		default:
			throw new UserPermException("The user doesn't have enough permissions to execute this action.");
		}
	}
	
	@Override
	public void restart() 
			throws TaskInProgress, InvalidState, RuntimeFault, RemoteException, InterruptedException, 
			UserPermException {
		
		switch (permission) {
		case ROOT:
		case ADMIN:		
			Task task = vm.resetVM_Task();
			if (task.waitForTask() == Task.SUCCESS) {
				log.debug("VM reset!");
			}
			break;			
		default:
			throw new UserPermException("The user doesn't have enough permissions to execute this action.");
		}
	}
	
	
}
