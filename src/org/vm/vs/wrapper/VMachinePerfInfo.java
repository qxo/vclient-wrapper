package org.vm.vs.wrapper;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.vm.vs.wrapper.perf.CpuPerfInfo;
import org.vm.vs.wrapper.perf.DiskAvgReadPerfInfo;
import org.vm.vs.wrapper.perf.DiskAvgWritePerfInfo;
import org.vm.vs.wrapper.perf.DiskReadPerfInfo;
import org.vm.vs.wrapper.perf.DiskWritePerfInfo;
import org.vm.vs.wrapper.perf.MemPerfInfo;
import org.vm.vs.wrapper.perf.NetInPerfInfo;
import org.vm.vs.wrapper.perf.NetOutPerfInfo;
import org.vm.vs.wrapper.perf.NetPerfInfo;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PropertyChange;
import com.vmware.vim25.PropertyChangeOp;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertyFilterUpdate;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.UpdateSet;
import com.vmware.vim25.VirtualMachineConnectionState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.mo.ManagedObject;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.PropertyCollector;
import com.vmware.vim25.mo.PropertyFilter;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.PropertyCollectorUtil;

/**
 * 
 * @author fender
 * 
 */
public class VMachinePerfInfo extends AbstractMachinePerfInfo {

	private static Logger log = Logger.getLogger(VMachinePerfInfo.class);
	
	protected ManagedEntityStatus vmStatus;
	protected VirtualMachinePowerState powerState;
	protected VirtualMachineConnectionState connState;
	protected Long memOverhead;
	protected PropertyCollector pc;
	protected int perfInterval;
	protected VirtualMachine vm;
	protected PerformanceManager perfMgr;
	protected HashMap<PerfMetricEnum, PerfMetricId> metrics = new HashMap<PerfMetricEnum, PerfMetricId>();
	
	
	/**
	 * Constructor.
	 * 
	 * @param vm
	 * @param pc
	 * @param pm
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public VMachinePerfInfo(ServiceInstance si, VirtualMachine vm) 
			throws InvalidProperty, RuntimeFault, RemoteException {
		
		this.pc = si.getPropertyCollector();
		this.vm = vm;
		this.perfMgr = si.getPerformanceManager();
		
		setPerformanceInfo();
		setAvailPerfMetrics();
	}

	
	@Override
	public void setPerformanceInfo() throws InvalidProperty, RuntimeFault, RemoteException {

		// @@ README:
        // "summary.quickStats" is taken out -- API ref says, 
        // "A set of statistics that are typically updated with near real-time regularity. 
        // This data object type does not support notification, for scalability reasons. 
        // Therefore, changes in QuickStats do not generate property collector updates. 
        // To monitor statistics values, use the statistics and alarms modules instead.		
		
		// String [][] typeInfo = { new String[] { "VirtualMachine", "name", "runtime" } };
		String[][] typeInfo = { new String[] { 
				"VirtualMachine", 
				"summary.quickStats"
			}
		};

		PropertySpec[] pSpecs = PropertyCollectorUtil.buildPropertySpecArray(typeInfo);
		ObjectSpec[] oSpecs = createObjectSpecs(vm);
		PropertyFilterSpec filterSpec = new PropertyFilterSpec();
		filterSpec.setPropSet(pSpecs);
		filterSpec.setObjectSet(oSpecs);

		String version = "";
		PropertyFilter pf = pc.createFilter(filterSpec, false);

		UpdateSet update = pc.checkForUpdates(version);

		if (update != null && update.getFilterSet() != null) {
			handleUpdate(update);
			version = update.getVersion();
//			log.debug("version is:" + version);
		} else {
			log.debug("No update is present!");
		}

		pf.destroyPropertyFilter();
	}

	/**
	 * 
	 * @param update
	 */
	private void handleUpdate(UpdateSet update) {
		ArrayList<ObjectUpdate> vmUpdates = new ArrayList<ObjectUpdate>();
		ArrayList<ObjectUpdate> hostUpdates = new ArrayList<ObjectUpdate>();
		PropertyFilterUpdate[] pfus = update.getFilterSet();

		for (int i = 0; i < pfus.length; i++) {
			ObjectUpdate[] ous = pfus[i].getObjectSet();

			for (ObjectUpdate ou : ous) {

				if (ou.getObj().getType().equals("VirtualMachine")) {
					vmUpdates.add(ou);
				} else if (ou.getObj().getType().equals("HostSystem")) {
					hostUpdates.add(ou);
				}
			}
		}

		for (ObjectUpdate ou : vmUpdates) {
//			log.debug("Virtual Machine updates:");
			handleObjectUpdate(ou);
		}

		for (ObjectUpdate hu : hostUpdates) {
			log.debug("Host updates:");
			handleObjectUpdate(hu);
		}
	}

	/**
	 * 
	 * @param oUpdate
	 */
	private void handleObjectUpdate(ObjectUpdate oUpdate) {
		PropertyChange[] pc = oUpdate.getChangeSet();
//		log.debug(oUpdate.getKind() + "Data:");
		handleChanges(pc);
	}

	/**
	 * 
	 * @param changes
	 */
	private void handleChanges(PropertyChange[] changes) {

		for (int i = 0; i < changes.length; i++) {
			String name = changes[i].getName();
			Object value = changes[i].getVal();
			PropertyChangeOp op = changes[i].getOp();

			if (op != PropertyChangeOp.remove) {
				
//				log.debug("  Property Name: " + name);

				if ("summary.quickStats".equals(name)) {

					if (value instanceof VirtualMachineQuickStats) {

						VirtualMachineQuickStats vmqs = (VirtualMachineQuickStats) value;
						
						cpuUsage = vmqs.getOverallCpuUsage() == null ? -1 : vmqs.getOverallCpuUsage().intValue();
						memUsage = vmqs.getGuestMemoryUsage() == null ? -1 : vmqs.getGuestMemoryUsage().intValue();
						swapUsage = vmqs.getSwappedMemory() == null ? -1 : vmqs.getSwappedMemory().intValue();
						vmStatus = vmqs.getGuestHeartbeatStatus() == null 
								? ManagedEntityStatus.gray : vmqs.getGuestHeartbeatStatus();
						
//						log.debug("   Guest Status: " + vmStatus.toString());
//						log.debug("   CPU Load MHz: " + cpuUsage);
//						log.debug("   Memory Load MB: " + memUsage);
//						log.debug("   Swap MB: " + swapUsage);
					}
										
				} else {
					log.debug("   " + value.toString());
				}

			} else {
				log.debug("Property Name: " + name + " value removed.");
			}
		}
	}

	/**
	 * Specify which metrics are going to be used to collect performance data. 
	 * 
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	private void setAvailPerfMetrics() throws RuntimeFault, RemoteException {
		
		PerfProviderSummary summary = perfMgr.queryPerfProviderSummary(vm);
		perfInterval = summary.getRefreshRate();
		PerfMetricId[] queryAvailablePerfMetric = perfMgr.queryAvailablePerfMetric(vm, null, null, perfInterval);
		
		if (queryAvailablePerfMetric == null) {
			log.debug("No available metrics found.");
			return;
		}
		
		for (int i = 0; i < queryAvailablePerfMetric.length; i++) {
			PerfMetricId perfMetricId = queryAvailablePerfMetric[i];

			PerfMetricEnum metricId = PerfMetricEnum.NONE;			
			metricId = metricId.getMetric(perfMetricId.getCounterId());
			
			// Selected metrics that will be used to gather performance info.
			switch (metricId) {			
			case CPU_USAGE:
			case MEM_USAGE:				
			case NET_USAGE:
			case NET_IN:
			case NET_OUT:
			case DISK_AVG_READ_REQ:
			case DISK_AVG_WRITE_REQ:
				//log.debug("+ perf metric id: " + perfMetricId.getCounterId());
				metrics.put(metricId, perfMetricId);
				break;

			default:
				//log.warn("The metric id that you try to use has not been added to the chosen ones list.");
			}
		}
	}
	
	/**
	 * 
	 * @param mo
	 * @return
	 */
	private ObjectSpec[] createObjectSpecs(ManagedObject mo) {
		ObjectSpec[] oSpecs = new ObjectSpec[] { new ObjectSpec() };
		oSpecs[0].setObj(mo.getMOR());
		oSpecs[0].setSkip(Boolean.FALSE);
		return oSpecs;
	}

	public VirtualMachineConnectionState getConnState() {
		return connState;
	}
	
	public Long getMemOverhead() {
		return memOverhead;
	}
	
	public VirtualMachinePowerState getPowerState() {
		return powerState;
	}
	
	public ManagedEntityStatus getVmStatus() {
		return vmStatus;
	}
	
	public HashMap<PerfMetricEnum, PerfMetricId> getMetrics() {
		return metrics;
	}
	
	public int getPerfInterval() {
		return perfInterval;
	}
	
	public PerformanceManager getPerfMgr() {
		return perfMgr;
	}
	
	public VirtualMachine getVm() {
		return vm;
	}
	
	@Override
	public Map<Date, Long> getCpuHistUsage() throws RuntimeFault, RemoteException {		
		return new CpuPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}


	@Override
	public Map<Date, Long> getMemHistUsage() throws Exception {
		return new MemPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}


	@Override
	public Map<Date, Long> getNetHistUsage() throws Exception {
		return new NetPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	@Override
	public Map<Date, Long> getNetInHist() throws Exception {
		return new NetInPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	@Override
	public Map<Date, Long> getNetOutHist() throws Exception {
		return new NetOutPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	@Override
	public Map<Date, Long> getDiskReads() throws Exception {
		return new DiskReadPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	@Override
	public Map<Date, Long> getDiskWrites() throws Exception {
		return new DiskWritePerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	public Map<Date, Long> getDiskAvgReads() throws Exception {
		return new DiskAvgReadPerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
	
	public Map<Date, Long> getDiskAvgWrites() throws Exception {
		return new DiskAvgWritePerfInfo(metrics, vm, perfInterval, perfMgr).getHistUsageData();
	}
		
}
