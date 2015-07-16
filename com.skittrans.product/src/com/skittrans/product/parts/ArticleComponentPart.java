package com.skittrans.product.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

import com.skittrans.services.IScanEvent;
import com.skittrans.services.IScanEventListener;
import com.skittrans.services.IScanEventManager;

/**
 * @author Evgeniy Vishnyakov
 */
public class ArticleComponentPart implements IScanEventListener {

	private DataBindingContext ctx = new DataBindingContext();
	private WritableValue selectedValue = new WritableValue();
	private WritableList input = new WritableList();
	private TableViewer tableViewer;
	
	@Inject
	private IScanEventManager scanEventManager;
	
	@Inject
	private UISynchronize uiSynchronize;
	private CLabel descEventLabel;
	private CLabel eventIDLabel;
	private CLabel readerNameLabel;
	
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.VERTICAL);
		createPart1(new Composite(sashForm, SWT.NONE));
		createPart2(new Composite(sashForm, SWT.NONE));
	    createBindings();
	    
	    scanEventManager.registerListener("group 2", "Jone Doe", this);
	    scanEventManager.registerListener("group 2", "Donald Duck", this);
	}
	
	@PreDestroy
	public void dispose() {
		ctx.dispose();
		scanEventManager.unregister(this);
	}
	
	private void createPart1(Composite parent) {
		parent.setLayout(new GridLayout());
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		parent.setLayout(tableColumnLayout);
		
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new ObservableListContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		
		TableColumn column = new TableColumn(tableViewer.getTable(), SWT.NONE);
	    column.setText("Reader");
	    tableColumnLayout.setColumnData(column, new ColumnWeightData(50, 150, true)); 

	    column = new TableColumn(tableViewer.getTable(), SWT.NONE);
	    column.setText("Article");
	    tableColumnLayout.setColumnData(column, new ColumnWeightData(50, 150, true));
		
		tableViewer.setInput(input);
	}
	
	private void createPart2(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).equalWidth(true).create());
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		String font = "big_font";
		FontData fontData = parent.getFont().getFontData()[0];
		fontData.height *= 2; 
		JFaceResources.getFontRegistry().put(font, new FontData[] { fontData });
		Font bigFont = JFaceResources.getFontRegistry().get(font);
		
		readerNameLabel = new CLabel(parent, SWT.LEFT);
		readerNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		readerNameLabel.setText(" ");
		readerNameLabel.setFont(bigFont);
		
		eventIDLabel = new CLabel(parent, SWT.BORDER | SWT.CENTER);
		eventIDLabel.setFont(bigFont);
		eventIDLabel.setText("   ");
		eventIDLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		eventIDLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		
		descEventLabel = new CLabel(parent, SWT.NONE);
		descEventLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 4, 1));
		descEventLabel.setText("sample article");
	}
	
	
	private void createBindings() {
		ctx.bindValue(ViewersObservables.observeSingleSelection(tableViewer), selectedValue);
		ctx.bindValue(WidgetProperties.text().observe(eventIDLabel), 
			PojoProperties.value("eventID").observeDetail(selectedValue));
		ctx.bindValue(WidgetProperties.text().observe(readerNameLabel),
			new ComputedValue() {
				@Override
				protected Object calculate() {
					Object value = selectedValue.getValue();
					if(null == value) {
						return "";
					}
					IScanEvent e = (IScanEvent) value;
					return String.format("%s ( %s )", e.getReaderName(), e.getGroup());
				}
			});
	}
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			IScanEvent e = (IScanEvent) element;
			String result = "";
			switch(columnIndex){
				case 0:
					result = String.format("%s ( %s )", e.getReaderName(), e.getGroup()) ;
					break;
				case 1:
					result = e.getEventID();
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
				input.add(event);
				selectedValue.setValue(event);
			}
		});
	}
	
	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
}
