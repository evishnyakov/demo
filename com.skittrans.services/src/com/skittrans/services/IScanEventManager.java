package com.skittrans.services;

/**
 * @author Evgeniy Vishnyakov
 */
public interface IScanEventManager {

	void registerListener(String groupName, String readerName, IScanEventListener eventListener);
	
	void unregister(IScanEventListener eventListener);
	
	IStatisticsInfo getStatisticsInfo();
}
