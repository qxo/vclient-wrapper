package org.vm.vs.wrapper;

import java.util.HashMap;

/**
 * 
 * @author fender
 *
 */
public enum PerfMetricEnum {
	
	NONE(0),
	CPU_USAGE(2), 				// cpu usage in % (rate)
	MEM_USAGE(6),				// mem usage in % (rate)
	NET_USAGE(100), 			// net usage in KBps (rate)
	NET_IN(105),				// data received in KBps (rate)
	NET_OUT(106),				// data transmitted in KBps (rate)
	DISK_AVG_READ_REQ(319),		// Average read requests per second (rate)
	DISK_AVG_WRITE_REQ(320),	// Average write requests per second (rate)
	DISK_READS(321),				// Read rate in KBps (rate)
	DISK_WRITES(322)				// Write rate in KBps (rate)
	;
	
	private int counterId;
	private static HashMap<Integer, PerfMetricEnum> lookup = new HashMap<Integer, PerfMetricEnum>();
	
	static {
		for (PerfMetricEnum m : PerfMetricEnum.values()) {
			lookup.put(m.getCounterId(), m);
		}
	}
	
	private PerfMetricEnum(int counterId) {
		this.counterId = counterId;
	}
	
	private int getCounterId() {
		return counterId;
	}
	
	public PerfMetricEnum getMetric(int counterId) {
		PerfMetricEnum metric = lookup.get(Integer.valueOf(counterId));		
		return metric != null ? metric : NONE;
	}
}
