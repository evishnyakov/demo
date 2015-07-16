package com.skittrans.services;

/**
 * @author Evgeniy Vishnyakov
 */
public interface IScanEvent {

	String getGroup();
	
	String getReaderName();
	
	String getEventID();
}
