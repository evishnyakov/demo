package com.skittrans.demo;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.skittrans.services.IStatisticsInfo;
import com.skittrans.services.impl.ScanEvent;

/**
 * @author Evgeniy Vishnyakov
 */
public class DemoStatisticsInfo implements IStatisticsInfo {
	private Map<String, Integer> map = Maps.newHashMap();
	@Override
	public List<String> getEventIDs() {
		synchronized (map) {
			return Lists.newArrayList(map.keySet());	
		}
	}

	@Override
	public Integer getCount(String eventID) {
		synchronized (map) {
			return map.get(eventID);
		}
	}

	public void process(ScanEvent event) {
		synchronized (map) {
			Integer count = map.get(event.getEventID());
			if(count == null) {
				count = 0;
			}
			map.put(event.getEventID(), count + 1);
		}
	}

	
	
}
