package org.vm.vs.wrapper.perf;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import org.vm.vs.wrapper.PerfMetricEnum;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * 
 * @author fender
 *
 */
public abstract class PerformanceInfo {

	protected static Logger log = Logger.getLogger(PerformanceInfo.class);
	
	protected Map<PerfMetricEnum, PerfMetricId> metrics;
	protected VirtualMachine vm;
	protected int perfInterval;
	protected PerformanceManager perfMgr;
	
	public PerformanceInfo(
			Map<PerfMetricEnum, PerfMetricId> metrics,
			VirtualMachine vm, 
			int perfInterval, 
			PerformanceManager perfMgr) {
		
		this.metrics = metrics;
		this.vm = vm;
		this.perfInterval = perfInterval;
		this.perfMgr = perfMgr;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract Map<Date, Long> getHistUsageData();
	
	
	/**
	 * 
	 * @param metricId
	 * @param valueType
	 * @return
	 * @throws RemoteException 
	 * @throws RuntimeFault 
	 */
	public Map<Date, Long> getPerfValues(PerfMetricEnum metricId, ValueTypeEnum valueType) 
			throws RuntimeFault, RemoteException {
			
		Map<Date, Long> result = new TreeMap<Date, Long>();
		PerfMetricId[] selectedPerfMetricIds = new PerfMetricId[1];

		if (!metrics.containsKey(metricId)) {
			return null;
		}
		
		selectedPerfMetricIds[0] = metrics.get(metricId);
		
		PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
		perfQuerySpec.setEntity(vm.getMOR());
		perfQuerySpec.setMetricId(selectedPerfMetricIds);
		perfQuerySpec.intervalId = perfInterval;
				
		PerfEntityMetricBase[] pembs = perfMgr.queryPerf(new PerfQuerySpec[] { perfQuerySpec });

		if (pembs == null) {
			log.info("Perf Entity Metric Base is null!");
			return null;
		}
		
		PerfEntityMetricBase perfEntity = pembs[0];
		PerfEntityMetric pem = (PerfEntityMetric) perfEntity;
		PerfMetricSeries[] values = pem.getValue();
		PerfSampleInfo[] infos = pem.getSampleInfo();

		for (int j = 0; values != null && j < values.length; ++j) {
			PerfMetricIntSeries metric = (PerfMetricIntSeries) values[j];
			long[] vals = metric.getValue();

			log.debug("METRIC ID: " + metric.getId().getCounterId());

			Date date;
			long value;
			
			for (int k = 0; k < vals.length; k++) {
				
				date = (Date) infos[k].getTimestamp().getTime();				
				value = (valueType == ValueTypeEnum.PERCENTAGE) ? (vals[k] / 100) : vals[k];				
				result.put(date, Long.valueOf(value));

				log.debug("perf info: (" + infos[k].getTimestamp().getTime() + " : " + value + ")");
			}
		}
		
		return result;
	}
	
}
