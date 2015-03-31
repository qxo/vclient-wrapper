package org.vm.vs.wrapper;

/**
 * 
 * @author fender
 *
 */
public interface Machine {

	void start() throws Exception;
	
	void stop() throws Exception;
	
	void restart() throws Exception;
	
	
}
