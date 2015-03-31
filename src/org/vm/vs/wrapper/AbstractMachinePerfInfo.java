package org.vm.vs.wrapper;



/**
 * 
 * @author fender
 *
 */
abstract public class AbstractMachinePerfInfo implements MachinePerfInfo {
	
	protected int cpuUsage = -1;
	protected int memUsage = -1;
	protected int swapUsage = -1;
	protected int diskUsage = -1;
	
	@Override
	public int getCpuUsage() {
		return cpuUsage;
	}

	@Override
	public int getMemUsage() {
		return memUsage;
	}

	@Override
	public int getDiskUsage() {
		return diskUsage;
	}

	@Override
	public int getSwapUsage() {
		return swapUsage;
	}
	
	/**
	 * Retrieves the machine info and sets the resources usage values. 
	 */
	abstract public void setPerformanceInfo() throws Exception;
		
}
