package org.vm.vs.wrapper;

import java.util.Date;
import java.util.Map;



/**
 * 
 * @author fender
 *
 */
public interface MachinePerfInfo {

	/**
	 * 
	 * @return
	 */
	int getCpuUsage();
	
	/**
	 * 
	 * @return memory usage in MB
	 */
	int getMemUsage();
	
	/**
	 * 
	 * @return disk usage in MB
	 */
	int getDiskUsage();
	
	/**
	 * 
	 * @return swap usage in MB
	 */
	int getSwapUsage();

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getCpuHistUsage() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getMemHistUsage() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getNetHistUsage() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getNetInHist() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getNetOutHist() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getDiskReads() throws Exception;
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	Map<Date, Long> getDiskWrites() throws Exception;
	
}
