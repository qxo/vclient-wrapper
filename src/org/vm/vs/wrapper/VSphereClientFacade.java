package org.vm.vs.wrapper;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

/**
 * 
 * @author fender
 *
 */
public class VSphereClientFacade {

    private static final Logger LOG = Logger.getLogger(VSphereClientFacade.class);

    private static VSphereClient vmc = null;

    static enum CmdEnum {
        printInfo, startVm, stopVm, restartVmIfCritical, restartVm;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 4) {
            System.out.println("<vcenter sdk url> <user> <password> <vm name> < "+Arrays.asList(CmdEnum.values())+" > ");
            return;
        }
        System.out.println("args:" + Arrays.asList(args));
        String vmName = args[3];
        VSphereClientFacade tool = new VSphereClientFacade();

        tool.login(args[0], args[1], args[2]);
        // printHostInfo(vmName);
        String cmd = args.length > 4 ? args[4] : null;
        if (cmd == null) {
            cmd = "printInfo";
        }
        cmd = cmd.trim();
        if (cmd.length() < 0) {
            cmd = "printInfo";
        }
        CmdEnum type = CmdEnum.valueOf(cmd);
        switch (type) {
        case startVm:
            tool.startVm(vmName);
            break;
        case stopVm:
            tool.stopVm(vmName);
            break;
        case restartVmIfCritical:
            tool.restartVmIfCritical(vmName);
            break;
        case restartVm:
            tool.restartVm(vmName);
            break;
        case printInfo:
        default:
            tool.printCpuHistUsage(vmName);
            tool.printMemHistUsage(vmName);
            tool.printNetHistUsage(vmName);
            tool.printNetInHist(vmName);
            tool.printNetOutHist(vmName);
            tool.printDiskReadsHist(vmName);
            tool.printDiskWritesHist(vmName);
        }

        // tool.restartVmIfCritical(vmName);
        // tool.startVm(vmName);
        // tool.restartVm(vmName);
        // tool.stopVm(vmName);

        tool.logout();

        LOG.info("over");
    }

    public VSphereClientFacade restartVmIfCritical(String vmName) {
        final JsonObject vmInfo = printHostInfo(vmName);
        final String status = vmInfo.getAsJsonObject("machine").get("status").getAsString();
        LOG.info("vm:" + vmName + " status:" + status);
        if ("CRITICAL".equals(status)) {
            restartVm(vmName);
        } else {
            LOG.info("vm:" + vmName + " no need for restart");
        }
        return this;
    }

    /**
     * Logs in to the vSphere service.
     */
    public VSphereClientFacade login(String host, String user, String passwd) {
        try {
            vmc = new VSphereClient(host, user, passwd);
            // vmc.loadManagedEntities();
            LOG.info("Login success!");
            return this;
        } catch (Exception e) {
            LOG.warn("Could not log in to vSphere.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of virtual machines with names and status.
     */
    public JsonObject printHostsList() {
        try {
            JsonObject machinesInfo = vmc.getMachinesSummaryInfo();
            LOG.debug("Machines info: " + machinesInfo.toString());
            return machinesInfo;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of virtual hosts.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieves the virtual machine info given a host name.
     */
    public static JsonObject printHostInfo(String vmName) {
        try {
            String remoteConsoleUrl = "remote-console-url";
            vmc.setRemoteConsoleUrl(remoteConsoleUrl);
            JsonObject machineInfo = vmc.getMachineInfo(vmName);
            LOG.info("Machine info: " + machineInfo.toString());
            return machineInfo;
        } catch (Exception e) {
            LOG.error("Could not retrieve the vm info.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of CPU usage values (percentage).
     */
    public JsonObject printCpuHistUsage(String vmName) {
        try {
            JsonObject cpuUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.CPU_USAGE);
            LOG.debug("cpuUsage: " + cpuUsage.toString());
            return cpuUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list CPU values.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of memory usage values (percentage).
     */
    public JsonObject printMemHistUsage(String vmName) {
        try {
            JsonObject memUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.MEM_USAGE);
            LOG.debug("memUsage: " + memUsage.toString());
            return memUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of Mem usage values.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of net usage values (absolute).
     */
    public JsonObject printNetHistUsage(String vmName) {
        try {
            JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_USAGE);
            LOG.debug("netUsage: " + netUsage.toString());
            return netUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of net usage values.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of net in traffic values (absolute).
     */
    public JsonObject printNetInHist(String vmName) {
        try {
            JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_IN);
            LOG.debug("netInUsage: " + netUsage.toString());
            return netUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of net (in) usage values", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of net out traffic values (absolute).
     */
    public JsonObject printNetOutHist(String vmName) {
        try {
            JsonObject netUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.NET_OUT);
            LOG.debug("netOutUsage: " + netUsage.toString());
            return netUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of net (out) usage values", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of average read requests form the virtual disk.
     */
    public JsonObject printDiskReadsHist(String vmName) {
        try {
            JsonObject diskUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.DISK_AVG_READ_REQ);
            LOG.debug("result (disk reads): " + diskUsage.toString());
            return diskUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of disk read requests.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a list of average write requests form the virtual disk.
     */
    public JsonObject printDiskWritesHist(String vmName) {
        try {
            JsonObject diskUsage = vmc.getVmPerfHistData(vmName, PerfMetricEnum.DISK_AVG_WRITE_REQ);
            LOG.debug("result (disk writes): " + diskUsage.toString());
            return diskUsage;
        } catch (Exception e) {
            LOG.error("Could not retrieve the list of disk write requests.", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Starts a virtual machine given its name.
     */
    public VSphereClientFacade startVm(String vmName) {
        VMachine vm = vmc.getVm(vmName);
        try {
            vm.start();
        } catch (Exception e) {
            LOG.error("The virtual machine could not be started.", e);
            throw new IllegalStateException(e);
        }
        return this;
    }

    /**
     * Stops a virtual machine given its name.
     */
    public VSphereClientFacade stopVm(String vmName) {
        VMachine vm = vmc.getVm(vmName);
        try {
            vm.stop();
        } catch (Exception e) {
            LOG.error("The virtual machine could not be stopped.", e);
            throw new IllegalStateException(e);
        }
        return this;
    }

    /**
     * Restarts a virtual machine given its name.
     */
    public VSphereClientFacade restartVm(String vmName) {
        LOG.info("restart vm:" + vmName);
        VMachine vm = vmc.getVm(vmName);
        try {
            vm.restart();
        } catch (Exception e) {
            LOG.error("The virtual machine could not be restarted.", e);
            throw new IllegalStateException(e);
        }
        return this;
    }

    /**
     * Sets a custom name for a given virtual machine.
     */
    public VSphereClientFacade setCustomName(String vmName, String newName) {
        try {
            vmc.setVmCustomName(vmName, newName);
        } catch (Exception e) {
            LOG.error("Couldn't set a custom name for the virtual machine.", e);
        }
        return this;
    }

    /**
     * Logs out the user from the vSphere service.
     */
    public VSphereClientFacade logout() {
        LOG.info("logout...");
        vmc.disconnect();
        return this;
    }

}
