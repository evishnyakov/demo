package com.skittrans.services;

import java.util.List;

/**
 * @author Evgeniy Vishnyakov
 */
public interface IStatisticsInfo {

	List<String> getEventIDs();
	
	Integer getCount(String eventID);
	
}
