package com.skittrans.demo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.skittrans.services.IScanEventListener;
import com.skittrans.services.IScanEventManager;
import com.skittrans.services.IStatisticsInfo;
import com.skittrans.services.impl.ScanEvent;

/**
 * @author Evgeniy Vishnyakov
 */
public class DemoScanEventManager implements IScanEventManager {

	private Object sync = new Object();
	private Table<String, String, List<IScanEventListener>> listeners = HashBasedTable.create();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private DemoStatisticsInfo statistics = new DemoStatisticsInfo(); 
	
	private Runnable listenersNotifier = new Runnable() {
		Random rand = new Random();
		public void run() {
			synchronized (sync) {
				for(Table.Cell<String, String, List<IScanEventListener>> cell : Lists.newArrayList(listeners.cellSet())) {
					ScanEvent event = new ScanEvent(cell.getRowKey(), cell.getColumnKey(), rand.nextInt(10) + "");
					statistics.process(event);
					for(IScanEventListener listener : cell.getValue()) {
						listener.notifyListener(event);
					}
				}
			}
		}
	};
	private ScheduledFuture<?> scheduleFuture;		   
	
	@Override
	public void registerListener(String groupName, String readerName, IScanEventListener eventListener) {
		if(null == eventListener) {
			return ;
		}
		synchronized (sync) {
			if(listeners.isEmpty()) {
				scheduleFuture = scheduler.scheduleAtFixedRate(listenersNotifier, 0, 5, TimeUnit.SECONDS);
			}
			List<IScanEventListener> list = listeners.get(groupName, readerName);
			if(null == list) {
				list = Lists.newArrayList();
				listeners.put(groupName, readerName, list);
			}
			list.add(eventListener);
		}
	}

	@Override
	public synchronized void unregister(IScanEventListener eventListener) {
		if(null == eventListener) {
			return ;
		}
		synchronized (sync) {
			for(Table.Cell<String, String, List<IScanEventListener>> cell : Lists.newArrayList(listeners.cellSet())) {
				List<IScanEventListener> list = listeners.get(cell.getRowKey(), cell.getColumnKey());
				list.remove(eventListener);
				if(list.isEmpty()) {
					listeners.remove(cell.getRowKey(), cell.getColumnKey());
				}
			}
			if(listeners.isEmpty() && scheduleFuture != null) {
				scheduleFuture.cancel(false);
				scheduleFuture = null;
			}
		}
	}
	
	@Override
	public IStatisticsInfo getStatisticsInfo() {
		return statistics;
	}

}
