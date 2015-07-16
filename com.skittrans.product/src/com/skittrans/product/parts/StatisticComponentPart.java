package com.skittrans.product.parts;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.google.common.collect.Lists;
import com.skittrans.services.IScanEvent;
import com.skittrans.services.IScanEventListener;
import com.skittrans.services.IScanEventManager;
import com.skittrans.services.IStatisticsInfo;

/**
 * @author Evgeniy Vishnyakov
 */
public class StatisticComponentPart implements IScanEventListener {

	@Inject
	private IScanEventManager scanEventManager;
	private TableViewer tableViewer;
	@Inject
	private UISynchronize uiSynchronize;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout());
		
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		parent.setLayout(tableColumnLayout);
		
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		TableColumn column = new TableColumn(tableViewer.getTable(), SWT.NONE);
	    column.setText("Article group");
	    tableColumnLayout.setColumnData(column, new ColumnWeightData(50, 150, true)); 

	    column = new TableColumn(tableViewer.getTable(), SWT.NONE);
	    column.setText("Count");
	    tableColumnLayout.setColumnData(column, new ColumnWeightData(50, 150, true));
		
		scanEventManager.registerListener("group 2", "Jone Doe", this);
	    scanEventManager.registerListener("group 2", "Donald Duck", this);
	}
	
	@PreDestroy
	public void dispose() {
		scanEventManager.unregister(this);
	}
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Pair e = (Pair) element;
			String result = "";
			switch(columnIndex){
				case 0:
					result = String.format("Article Group %s", e.s) ;
					break;
				case 1:
					result = e.i.toString();
					break;
				default:
					//should not reach here
					result = "";
			}
			return result;
		}
	}
	
	@Override
	public void notifyListener(final IScanEvent event) {
		uiSynchronize.asyncExec(new Runnable() {
			@Override
			public void run() {
				IStatisticsInfo statisticsInfo = scanEventManager.getStatisticsInfo();
				List<Pair> pairs = Lists.newArrayList();
				for(String eventID : statisticsInfo.getEventIDs()) {
					pairs.add(new Pair(eventID, statisticsInfo.getCount(eventID)));
				}
				tableViewer.setInput(pairs);
			}
		});
	}
	
	private class Pair {
		String s;
		Integer i;
		public Pair(String s, Integer i) {
			this.s = s;
			this.i = i;
		}
	}
	
	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
}
