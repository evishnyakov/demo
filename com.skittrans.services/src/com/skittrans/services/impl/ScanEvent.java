package com.skittrans.services.impl;

import com.skittrans.services.IScanEvent;

/**
 * @author Evgeniy Vishnyakov
 */
public class ScanEvent implements IScanEvent {

	private String group;
	private String readerName;
	private String id;

	public ScanEvent(String group, String readerName, String id) {
		this.group = group;
		this.readerName = readerName;
		this.id = id;
	}
	
	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getReaderName() {
		return readerName;
	}

	@Override
	public String getEventID() {
		return id;
	}
	
	@Override
	public String toString() {
		return "ScanEvent ['"+readerName+"' , '"+group+"', '"+id+"'] ";
	}

}
