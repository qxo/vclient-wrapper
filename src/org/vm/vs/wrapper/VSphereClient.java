package org.vm.vs.wrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.vm.vs.wrapper.perf.PerfInfoFactory;
import org.vm.vs.wrapper.perf.PerformanceInfo;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * This class is used to connect to vSphere service and handle remote virtual machines.
 * 
 * @author fender
 *
 */
public final class VSphereClient {

	private static Logger log = Logger.getLogger(VSphereClient.class);
	
	private ServiceInstance si;
	private ManagedEntity[] managedEntities;
	
	/**
	 * Virtual machine managed entity.
	 */
	private static String ME_VM_MACHINE = "VirtualMachine";
	
    /**
     * Virtual Center instance id.
     */
    private String vcInstanceUUID;	
    
    /**
     * URL to open a remote console.
     */
    private String remoteConsoleUrl;
    

    /**
     * 
     * @param name
     * @param mes
     * @return
     */
    private static VMachine getVmByName(String name, ManagedEntity[] mes) {
        for (ManagedEntity entity : mes) {
            try {
            	VirtualMachine vm = (VirtualMachine) entity;
            	if (vm == null) {
            		continue;
            	}
            	
            	if (vm.getName().equals(name)) {
            		VMachine vmachine = new VMachine(vm);
                    return vmachine;
            	}
                
            } catch (Exception e) {
                log.error("Error trying to retrieve the VM: " + e);
                e.printStackTrace();
            }
        }
        
        return null;
    }    
    
	/**
	 * Constructor
	 */
	public VSphereClient() {
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param host
	 * @param user
	 * @param passwd
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public VSphereClient(String host, String user, String passwd) throws RemoteException, MalformedURLException {
		connect(host, user, passwd);
		loadManagedEntities();
	}
	
	/**
	 * Creates a connection to the vSphere service.
	 * 
	 * @param host vSphere address
	 * @param user
	 * @param passwd
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public void connect(String host, String user, String passwd) throws RemoteException, MalformedURLException {
		log.info("new URL(host): " + new URL(host));
		this.si = new ServiceInstance(new URL(host), user, passwd, true);
        this.vcInstanceUUID = si.getServiceContent().getAbout().getInstanceUuid();        
		log.info("Connected to vSphere");
	}
	
	/**
	 * Loads all the managed entities that belong to the logged user.
	 * 
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public void loadManagedEntities() throws InvalidProperty, RuntimeFault, RemoteException {
		Folder rootFolder = si.getRootFolder();
		managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities(ME_VM_MACHINE);
	}
	
	/**
	 * Returns a list of virtual machines info.
	 * 
	 * @return
	 */
	public JsonObject getMachinesInfo() {
		JsonObject machinesInfo = new JsonObject();
        JsonArray machines = new JsonArray();        
        
        for (ManagedEntity entity : managedEntities) {
            try {
            	VMachineInfo vmi = new VMachineInfo(si, (VirtualMachine) entity);
    			machines.add(vmi.toJson());
    			
            } catch (Exception e) {
                log.error("Exception: " + e);
                machinesInfo.addProperty("success", false);
                return machinesInfo;
            }
        }
        
        machinesInfo.addProperty("success", true);
        machinesInfo.addProperty("result", machines.size());
        machinesInfo.add("machines", machines);
		
		return machinesInfo;
	}
	
	/**
	 * Returns a virtual machine info.
	 * 
	 * @param vmName
	 * @return
	 */
	public JsonObject getMachineInfo(String vmName) {
		JsonObject machineInfo = new JsonObject();

		try {
			VMachineInfo vmi = getVmInfoByName(vmName);
			
			if (vmi == null) {
				log.debug("VMI is null");
		        machineInfo.addProperty("success", false);
		        return machineInfo;				
			}
						
			vmi.setVcInstanceUUID(vcInstanceUUID);
			vmi.setRemoteConsoleUrl(remoteConsoleUrl);
			
			machineInfo.addProperty("success", true);
			machineInfo.add("machine", vmi.toJson());
			
			return machineInfo;

		} catch (Exception e) {
			log.error("Error trying to retrieve the VM info: " + e);
			e.printStackTrace();
		}        
        
        machineInfo.addProperty("success", false);
        return machineInfo;
	}	
	
	/**
	 * Gets the virtual machines names and their status.
	 * 
	 * @return
	 */
	public JsonObject getMachinesSummaryInfo() {
		JsonObject machinesInfo = new JsonObject();
        JsonArray machines = new JsonArray();
        
        for (ManagedEntity entity : managedEntities) {
            try {
            	VirtualMachine vm = (VirtualMachine) entity;
            	
            	VirtualMachineRuntimeInfo runtimeInfo = vm.getRuntime();
            	MachinePowerStateEnum powerState = MachinePowerStateEnum.STOPPED;
            	String vmState = powerState.getState(runtimeInfo.getPowerState()).toString();
            	
            	String customName = VUtil.getCustomAttributeValue(vm, VMachineInfo.VM_CFIELD_SRV_IDENTIFIER_NAME);
            	String name = (customName.equals("")) ? vm.getName() : customName;
            	
            	JsonObject machineInfo = new JsonObject();
            	machineInfo.addProperty("name", name);
            	machineInfo.addProperty("id", vm.getName());
            	machineInfo.addProperty("powerstate", vmState);
    			machines.add(machineInfo);
    			
            } catch (Exception e) {
                log.error("Exception: " + e);
                machinesInfo.addProperty("success", false);
                return machinesInfo;
            }
        }
        
        machinesInfo.addProperty("success", true);
        machinesInfo.addProperty("result", machines.size());
        machinesInfo.add("machines", machines);
		
		return machinesInfo;
	}	
	
	/**
	 * Returns the historical usage data for a given metric.
	 * 
	 * @param vmName virtual machine name
	 * @param metric type of collected data (cpu, mem, disk, net, etc).
	 * @return
	 */
	public JsonObject getVmPerfHistData(String vmName, PerfMetricEnum metric) {
		JsonObject result = new JsonObject();
		JsonArray array = new JsonArray();
				
		try {
			VMachineInfo vmi = getVmInfoByName(vmName);
			
			if (vmi == null) {
				log.debug("VMI is null");
				result.addProperty("success", false);
				return result;
			}
		
			PerfInfoFactory factory = PerfInfoFactory.getInstance();
			PerformanceInfo perfInfo = factory.build(metric, vmi);
			Map<Date, Long> values = perfInfo.getHistUsageData();
			
			array = toJsonArray(values);
			
			result.addProperty("success", true);
			result.addProperty("result", array.size());
			result.add("values", array);
			
			return result;
			
		} catch (Exception e) {
			log.error("Error trying to retrieve the VM info: " + e);
			e.printStackTrace();
		}        
		
		result.addProperty("success", false);		
		return result;
	}	
		
	/**
	 * Disconnects from the vSphere service.
	 */
	public void disconnect() {
		si.getServerConnection().logout();
	}
	
	/**
	 * Returns a virtual machine.
	 * 
	 * @param name
	 * @return
	 */
	public VMachine getVm(String name) {
		return getVmByName(name, managedEntities); 
	}
	
	/**
	 * 
	 * @return virtual center instance id
	 */
	public String getVcInstanceUUID() {
		return vcInstanceUUID;
	}
	
	/**
	 * 
	 * @param remoteConsoleUrl
	 */
	public void setRemoteConsoleUrl(String remoteConsoleUrl) {
		this.remoteConsoleUrl = remoteConsoleUrl;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getRemoteConsoleUrl() {
		return remoteConsoleUrl;
	}
	
	/**
	 * Sets a custom name for the virtual machine.
	 * 
	 * @param vmName virtual machine name
	 * @param customName custom name
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public void setVmCustomName(String vmName, String customName) throws RuntimeFault, RemoteException {
		VMachineInfo vmi = getVmInfoByName(vmName);
		vmi.setCustomName(customName);
	}

    /**
     * 
     * @param name virtual machine name
     * @return
     */
    private VMachineInfo getVmInfoByName(String name) {
    	VMachineInfo vmi;
    	
    	for (ManagedEntity entity : managedEntities) {
    		try {
    			VirtualMachine vm = (VirtualMachine) entity;
    			
    			if (vm == null) {
    				continue;
    			}
    			
    			if (vm.getName().equals(name)) {
    				vmi = new VMachineInfo(si, vm);
    				return vmi;
    			}
    			
    		} catch (Exception e) {
    			log.error("Error trying to retrieve the VM info: " + e);
    			e.printStackTrace();
    		}
    	}
    	
    	return null;
    }
	
	/**
	 * Converts from Map to Json Array.  
	 * 
	 * @param values performance values of a metric (cpu, mem, net, etc)
	 * @return
	 */
	public static JsonArray toJsonArray(Map<Date, Long>values) {
		JsonArray array = new JsonArray();
		JsonObject el;
				
		if (values == null) {
			el = new JsonObject();
			el.addProperty("time", "");
			el.addProperty("usage", "");
			array.add(el);
			return array;
		}
		
		for (Date d : values.keySet()) {
			Long v = values.get(d);
			
			el = new JsonObject();
			el.addProperty("time", VUtil.dateFormatter(d));
			el.addProperty("usage", v);
			array.add(el);
		}
		
		return array;
	}

}
