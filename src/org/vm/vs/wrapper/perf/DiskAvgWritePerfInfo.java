package org.vm.vs.wrapper.perf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.vm.vs.wrapper.PerfMetricEnum;
import org.vm.vs.wrapper.VMachineInfo;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * @author fender
 *
 */
public class DiskAvgWritePerfInfo extends PerformanceInfo {	
	
	/**
	 * 
	 * @param metrics
	 * @param vm
	 * @param perfInterval
	 * @param perfMgr
	 */
	public DiskAvgWritePerfInfo(
			Map<PerfMetricEnum, PerfMetricId> metrics, 
			VirtualMachine vm, 
			int perfInterval, 
			PerformanceManager perfMgr) {
		
		super(metrics, vm, perfInterval, perfMgr);
	}
	
	@Override
	public Map<Date, Long> getHistUsageData() {
		try {
			
			return getPerfValues(PerfMetricEnum.DISK_AVG_WRITE_REQ, ValueTypeEnum.RATE);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
