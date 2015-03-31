package org.vm.vs.wrapper.perf;

import org.apache.log4j.Logger;

import org.vm.vs.wrapper.PerfMetricEnum;
import org.vm.vs.wrapper.VMachineInfo;
import org.vm.vs.wrapper.VMachinePerfInfo;

/**
 * Singleton Factory with lazy intilialization.
 * 
 * @author fender
 *
 */
public class PerfInfoFactory {

	private static Logger log = Logger.getLogger(PerfInfoFactory.class);

	// This class will be instanced once getInstance() is called.
	private static class Factory {
		private static PerfInfoFactory INSTANCE = new PerfInfoFactory();
	}
	
	public static PerfInfoFactory getInstance() {
		return Factory.INSTANCE;
	}
	
	public PerformanceInfo build(PerfMetricEnum metric, VMachineInfo vm) {
		
		VMachinePerfInfo vmPerfInfo = vm.getPerformanceInfo();
		
		switch (metric) {
		
		case CPU_USAGE:
			return new CpuPerfInfo(			
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case MEM_USAGE:
			return new MemPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case NET_USAGE:
			return new NetPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case NET_IN:
			return new NetInPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case NET_OUT:
			return new NetOutPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case DISK_AVG_READ_REQ:
			return new DiskAvgReadPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case DISK_AVG_WRITE_REQ:			
			return new DiskAvgWritePerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case DISK_READS:
			return new DiskReadPerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		case DISK_WRITES:
			return new DiskWritePerfInfo(
					vmPerfInfo.getMetrics(), 
					vmPerfInfo.getVm(), 
					vmPerfInfo.getPerfInterval(), 
					vmPerfInfo.getPerfMgr());
			
		default:
			log.warn("Metric not implemented!");
		}
		
		return null;
	}
}
