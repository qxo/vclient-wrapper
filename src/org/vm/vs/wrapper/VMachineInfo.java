package org.vm.vs.wrapper;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSummary;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineToolsStatus;
import com.vmware.vim25.mo.CustomFieldsManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * This is a Proxy class that acts as an interface of the operations that can be executed over virtual machines
 * managed by vSphere server.
 * 
 * @author fender
 *
 */
public class VMachineInfo implements
		MachineInfo<MachinePowerStateEnum, MachineHeartbeatStatusEnum, VMachinePerfInfo> {

	private static Logger log = Logger.getLogger(VMachineInfo.class);
	
	protected VirtualMachine vm;	
    protected VirtualMachineConfigInfo config;
    protected VirtualMachineRuntimeInfo runtimeInfo;
    protected VirtualMachineConfigSummary summaryConfig;
    protected VMachinePerfInfo perfInfo;
    
    protected static MachineHeartbeatStatusEnum vmStatus = MachineHeartbeatStatusEnum.UNKNOWN;
    protected static MachinePowerStateEnum powerState = MachinePowerStateEnum.STOPPED;
    
    /**
     * Custom fields keys and names.
     */
    public static String VM_CFIELD_SRV_IDENTIFIER_NAME = "p_SvrNameCust";
    public static int VM_CFIELD_SRV_IDENTIFIER_KEY = 17;
    
    public static String VM_CFIELD_CONTROL_MASK_NAME = "p_ControlMask";
    public static int VM_CFIELD_CONTROL_MASK_KEY = 456;
    
    /**
     * Virtual Center instance id.
     */
    protected String vcInstanceUUID;	
    
    /**
     * Virtual machine id (MOR ref id).
     */
    protected String vmId;
    
    /**
     * URL to open a remote console.
     */
    protected String remoteConsoleUrl;    

        
	/**
	 * Constructor.
	 * 
	 * @param si
	 * @param vm
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public VMachineInfo(ServiceInstance si, VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {
		this.vm = vm;		
        this.config = vm.getConfig();
        this.runtimeInfo = vm.getRuntime();
		this.vmId = vm.getMOR().getVal();				
		this.summaryConfig = vm.getSummary().getConfig();
		this.perfInfo = new VMachinePerfInfo(si, vm);
	}
		
	@Override
	public MachinePowerStateEnum getPowerState() {
		VirtualMachinePowerState state = runtimeInfo.getPowerState();
		return (state == null) ? MachinePowerStateEnum.STOPPED : VMachineInfo.powerState.getState(state);
	}
	
	@Override
	public MachineHeartbeatStatusEnum getStatus() {
		return VMachineInfo.vmStatus.getVmStatus(perfInfo.getVmStatus());
	}
	
	@Override
	public String getName() {
		return vm.getName();
	}
	
	@Override
	public String getOSName() {
		return config.getGuestFullName();
	}
	
	@Override
	public long getUptime() {
		Calendar cal = runtimeInfo.getBootTime();
		
		if (cal == null) {
			log.info("The boot time could not be retrieved");
			return -1;
		}
		
		long bootime = cal.getTimeInMillis();
		long now = Calendar.getInstance().getTimeInMillis();
		return now - bootime;
	}
	
	@Override
	public String getIpAddr() {
		// @@ FIXME: should I retrieve all the IP addresses ?
		
		// @@ FIXME: the GuestInfo is available only when the vm is running !!! so I should get the IP info from
		// other object.
						
		String ipAddr = vm.getGuest().getIpAddress();
		return (ipAddr == null) ? "" : ipAddr;
	}
	
	@Override
	public String getMacAddr() {
		
		VirtualDevice [] devs = null;
		
		try {
			devs = config.getHardware().getDevice();
		} catch (Exception ignored) {
			
		}
		
		if (devs == null) {
			return "";
		}

		for (int i = 0; i < devs.length; i++) {
			if (devs[i] instanceof VirtualEthernetCard) {
				VirtualEthernetCard card = (VirtualEthernetCard) devs[i];
				return card.getMacAddress();
			}
		}

		return "";
	}
	
	@Override
	public HashMap<String, Integer> getMemory() {
		HashMap<String, Integer> mem = new HashMap<String, Integer>();
		mem.put("size", summaryConfig.getMemorySizeMB());
		mem.put("usage", perfInfo.getMemUsage());
		mem.put("swap", perfInfo.getSwapUsage());
		return mem;
	}
	
	@Override
	public ArrayList<HashMap<String, Integer>> getCpus() {
		
		// @@ FIXME: I should retrieve all the available CPUs !
	
		//int cpuNum = config.getHardware().getNumCPU();
		
		ArrayList<HashMap<String, Integer>> cpus = new ArrayList<HashMap<String, Integer>>();
		HashMap<String, Integer> cpu = new HashMap<String, Integer>();
		
		int usage = perfInfo.getCpuUsage();
		int max = runtimeInfo.getMaxCpuUsage();
		int avg = max >0 ?  (usage * 100) / max : -1;
		
		cpu.put("usage", perfInfo.getCpuUsage()); // in MHz
		cpu.put("usageAvg", avg); // in %
		cpu.put("max", max); // in MHz
		cpus.add(cpu);
		
		return cpus;
	}
	
	   
	@Override
	public ArrayList<HashMap<String, Object>> getDisks() {
		
		ArrayList<HashMap<String, Object>> dis = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> diskInfo = new HashMap<String, Object>();
		
		//
		// @@ README: with the code below you can get the real disk size, but you can't get the disk usage. The number
		// that you get is the disk size plus the part used by the configuration files, vm files, etc.
		//
		
		VirtualDevice [] devices = config.getHardware().getDevice();
		for (VirtualDevice device : devices) {
			if (device instanceof VirtualDisk) {				
				VirtualDisk disk = (VirtualDisk) device;
				
				long diskSize = (disk.getCapacityInKB() / 1024) / 1024; // in GB
				//log.debug("disk: " + diskSize + " GB");

				diskInfo.put("name", disk.getDeviceInfo().getLabel());
				diskInfo.put("size", diskSize);
				diskInfo.put("usage", -1);
				dis.add(diskInfo);
			}
		}
		
		//
		// @@ README: if you use the code below, you can get the disk usage, however the disk size that you get 
		// is smaller than the real disk capacity. That's because you get rid of the configuration files, vm
		// files, etc.
		//
		
//		GuestDiskInfo [] disks = vm.getGuest().getDisk();		
//		long used, free, capacity;
//		
//		for (int i = 0; i < disks.length; i++) {
//			
//			capacity = disks[i].getCapacity() / 1024 / 1024 / 1024;
//			free = disks[i].getFreeSpace() / 1024 / 1024 / 1024;
//			used = capacity - free;
//
//			log.debug("capacity: " + capacity);
//			log.debug("free: " + free);			
//			log.debug("usage: " + used);			
//			
//			diskInfo.put("name", disks[i].getDiskPath());
//			diskInfo.put("size", capacity);
//			diskInfo.put("usage", used);
//			dis.add(diskInfo);
//		}
		
		return dis;
	}	
	
	/**
	 * 
	 * @return  the creation date in milliseconds
	 */
	public long getCreationDate() {
		// @@ TODO: implement this !!		
		return 0;
	}	
	
	@Override
	public VMachinePerfInfo getPerformanceInfo() {
		return perfInfo;
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject vmInfo = new JsonObject();
		
        vmInfo.addProperty("name", this.getName());
        vmInfo.addProperty("os", this.getOSName());
        vmInfo.addProperty("powerstate", this.getPowerState().toString());
        vmInfo.addProperty("status", this.getStatus().toString());
        vmInfo.addProperty("uptime", this.getUptime());
        vmInfo.addProperty("ip", this.getIpAddr());
        vmInfo.addProperty("mac", this.getMacAddr());
        vmInfo.addProperty("controlmask", VUtil.getAccessControlPerm(vm).getId());
        
        try {
        	vmInfo.addProperty("identifier", VUtil.getCustomAttributeValue(vm, VM_CFIELD_SRV_IDENTIFIER_NAME));
        } catch (Exception e) {
        	log.info("The virtual machine identifier could not be retrieved.", e);
        	vmInfo.addProperty("identifier", "");
        }
        
        // Converts from array list to json.
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
    	
        HashMap<String, Integer> memValues = this.getMemory();
        if (memValues != null) {
            String memStr = gson.toJson(memValues);        
            JsonElement mem = parser.parse(memStr);
            vmInfo.add("mem", mem);        	
        } else {
        	vmInfo.addProperty("mem", "");
        }
        
        ArrayList<HashMap<String, Object>> diskValues = this.getDisks();
        if (diskValues != null) {
            String disksStr = gson.toJson(diskValues);
            JsonElement disks = parser.parse(disksStr);
            vmInfo.add("disks", disks);
        } else {
        	vmInfo.addProperty("disks", "");
        }

        ArrayList<HashMap<String, Integer>> cpuValues = this.getCpus();
        if (cpuValues != null) {
            String cpusStr = gson.toJson(cpuValues);
            JsonElement cpus = parser.parse(cpusStr);                    
            vmInfo.add("cpus", cpus);
        } else {
        	vmInfo.addProperty("cpus", "");
        }

        // Sets the URL to open a remote console.
		vmInfo.addProperty("remoteUrl", this.getRemoteUrl());
        
        return vmInfo;
	}
	
	/**
	 * 
	 * @return the URL to open a remote console
	 */
	public String getRemoteUrl() {
	   String remoteUrl = "{vcUrl}?vm={vcId}:VirtualMachine:{vmId}";
	    if(vcInstanceUUID == null){
	        remoteUrl = "{vcUrl}?VirtualMachine:{vmId}";	    
	    }else{
	        remoteUrl = remoteUrl.replace("{vcId}", vcInstanceUUID);        
	    }	
		remoteUrl = remoteUrl.replace("{vcUrl}", remoteConsoleUrl);
		remoteUrl = remoteUrl.replace("{vmId}", vmId);
		return remoteUrl;
	}
	
	/**
	 * 
	 * @return the virtual machine id
	 */
	public String getVmId() {
		return vmId;
	}
	
	/**
	 * 
	 * @param vcInstanceUUID
	 */
	public void setVcInstanceUUID(String vcInstanceUUID) {
		this.vcInstanceUUID = vcInstanceUUID;
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
	 * @param name
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public void setCustomName(String name) throws RuntimeFault, RemoteException {
		VUtil.setCustomAttr(vm, VM_CFIELD_SRV_IDENTIFIER_NAME, name);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCustomName() {
		try {
			return VUtil.getCustomAttributeValue(vm, VM_CFIELD_SRV_IDENTIFIER_NAME);
		} catch (Exception ingnored) {
			log.info("The virtual machine custom name could not be retrieved.");
			return "";
		}
	}

	
}
