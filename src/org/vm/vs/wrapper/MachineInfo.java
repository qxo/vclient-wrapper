package org.vm.vs.wrapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;

/**
 * Interface that defines the operations to retrieve machine information and update its configuration.
 * 
 * @author fender
 *
 * @param <S> machine state
 * @param <H> machine heartbeat status
 * @param <P> machine performance info
 */
public interface MachineInfo <S, H, P> {

	/**
	 * 
	 * @return machine status
	 */
	S getPowerState();
	
	/**
	 * 
	 * @return
	 */
	H getStatus();
	
	/**
	 * 
	 * @return machine name
	 */
	String getName();
	
	/**
	 * 
	 * @return OS name
	 */
	String getOSName();

	/**
	 * 
	 * @return the uptime in milliseconds
	 */
	long getUptime();
	
	/**
	 * 
	 * @return IP address
	 */
	String getIpAddr();
	
	/**
	 * 
	 * @return MAC address
	 */
	String getMacAddr();
	
	/**
	 * 
	 * @return the amount of RAM memory
	 */
	HashMap<String, Integer> getMemory();
	
	/**
	 * 
	 * @return CPUs info
	 */
	ArrayList<HashMap<String, Integer>> getCpus();
	
	/**
	 * 
	 * @return disks info 
	 */
	ArrayList<HashMap<String, Object>> getDisks();
	
	/**
	 * 
	 * @return
	 */
	P getPerformanceInfo();
	
	/**
	 * 
	 * @return machine info in JSON format
	 */
	JsonObject toJson();	
}
