import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import org.vm.vs.wrapper.PerfMetricEnum;
import org.vm.vs.wrapper.VMachine;
import org.vm.vs.wrapper.VSphereClient;


/**
 * 
 * @author fender
 *
 */
public class Examples {

	private static final Logger Log = Logger.getLogger(Examples.class);
	private static VSphereClient vmc = null;	


    public static void main(String[] args) {
    	String vmName = "virtual-machine-name";
    	
    	loginVm("http://localhost", "user", "password");
    	
    	printHostInfo(vmName);
    	printHostInfo(vmName);
    	printCpuHistUsage(vmName);
    	printMemHistUsage(vmName);
    	printNetHistUsage(vmName);
    	printNetInHist(vmName);
    	printNetOutHist(vmName);
    	printDiskReadsHist(vmName);
    	printDiskWritesHist(vmName);

//    	startVm(vmName);
//    	restartVm(vmName);
//    	stopVm(vmName);
    	
    	logoutVm();
    }
	
	/**
	 * Logs in to the vSphere service.
	 */
	public static void loginVm(String host, String user, String passwd) {
    	try {
			vmc = new VSphereClient(host, user, passwd);
			vmc.loadManagedEntities();
			Log.debug("Login success!");
		}
        catch (Exception e) {
			Log.warn("Could not log in to vSphere.", e);
		}
	}
	
	/**
	 * Returns a list of virtual machines with names and status.
	 */
	public static void printHostsList() {
		try {
			JsonObject machinesInfo = vmc.getMachinesSummaryInfo();
			Log.debug("Machines info: " + machinesInfo.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of virtual hosts.", e);			
		}
	}
	
	/**
	 * Retrieves the virtual machine info given a host name.
	 */
	public static void printHostInfo(String vmName) {
		try {
            String remoteConsoleUrl = "remote-console-url";
			vmc.setRemoteConsoleUrl(remoteConsoleUrl);
			JsonObject machineInfo = vmc.getMachineInfo(vmName);
			Log.debug("Machine info: " + machineInfo.toString());			
		}
        catch (Exception e) {
			Log.error("Could not retrieve the vm info.", e);			
		}
	}
	
	/**
	 * Returns a list of CPU usage values (percentage). 
	 */
	public static void printCpuHistUsage(String vmName) {
		try {
			JsonObject cpuUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.CPU_USAGE);
			Log.debug("cpuUsage: " + cpuUsage.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list CPU values.", e);			
		}
	}
	
	/**
	 * Returns a list of memory usage values (percentage).
	 */
	public static void printMemHistUsage(String vmName) {
		try {
			JsonObject memUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.MEM_USAGE);
			Log.debug("memUsage: " + memUsage.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of Mem usage values.", e);			
		}
	}
	
	/**
	 * Returns a list of net usage values (absolute).
	 */
	public static void printNetHistUsage(String vmName) {
		try {
			JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_USAGE);
			Log.debug("netUsage: " + netUsage.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of net usage values.", e);			
		}
	}
	
	/**
	 * Returns a list of net in traffic values (absolute).
	 */
	public static void printNetInHist(String vmName) {
		try {
			JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_IN);
			Log.debug("netInUsage: " + netUsage.toString());			
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of net (in) usage values", e);			
		}
	}
	
	/**
	 * Returns a list of net out traffic values (absolute).
	 */
	public static void printNetOutHist(String vmName) {
		try {
			JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_OUT);
			Log.debug("netOutUsage: " + netUsage.toString());			
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of net (out) usage values", e);			
		}
	}
	
	/**
	 * Returns a list of average read requests form the virtual disk.
	 */
	public static void printDiskReadsHist(String vmName) {
		try {
			JsonObject diskUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.DISK_AVG_READ_REQ);
			Log.debug("result (disk reads): " + diskUsage.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of disk read requests.", e);			
		}
	}
		
	/**
	 * Returns a list of average write requests form the virtual disk.
	 */
	public static void printDiskWritesHist(String vmName) {
		try {
			JsonObject diskUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.DISK_AVG_WRITE_REQ);
			Log.debug("result (disk writes): " + diskUsage.toString());
		}
        catch (Exception e) {
			Log.error("Could not retrieve the list of disk write requests.", e);			
		}
	}	
	
	/**
	 * Starts a virtual machine given its name.
	 */
	public static void startVm(String vmName) {
		VMachine vm = vmc.getVm(vmName);		
		try {
			vm.start();
		} catch (Exception e) {
			Log.error("The virtual machine could not be started.", e);
		}
	}
	
	/**
	 * Stops a virtual machine given its name.
	 */
	public static void stopVm(String vmName) {
		VMachine vm = vmc.getVm(vmName);	
		try {
			vm.stop();
		} catch (Exception e) {			
			Log.error("The virtual machine could not be stopped.", e);
		}
	}
	
	/**
	 * Restarts a virtual machine given its name.
	 */
	public static void restartVm(String vmName) {
		VMachine vm = vmc.getVm(vmName);		
		try {
			vm.restart();
		} catch (Exception e) {
			Log.error("The virtual machine could not be restarted.", e);
		}
	}
	
	/**
	 * Sets a custom name for a given virtual machine.
	 */
	public static void setCustomName(String vmName, String newName) {
		try {
			vmc.setVmCustomName(vmName, newName);
		} catch (Exception e) {
			Log.error("Couldn't set a custom name for the virtual machine.", e);
		}
	}
		
	/**
	 * Logs out the user from the vSphere service.
	 */
	public static void logoutVm() {
		vmc.disconnect();
	}
	
}
