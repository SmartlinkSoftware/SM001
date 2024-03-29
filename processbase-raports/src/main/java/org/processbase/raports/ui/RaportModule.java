package org.processbase.raports.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.processbase.raports.birt.generator.ReportItem;
import org.processbase.raports.data.ReportingDataStore;
import org.processbase.ui.core.Constants;
import org.processbase.ui.core.ProcessbaseApplication;
import org.processbase.ui.core.template.IPbTable;
import org.processbase.ui.core.template.PbWindow;
import org.processbase.ui.core.template.TableLinkButton;
import org.processbase.ui.osgi.PbPanelModule;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class RaportModule extends PbPanelModule implements IPbTable,  Button.ClickListener{

	private static Logger log=Logger.getLogger(RaportModule.class.getName());
	
	protected Table table = new Table();
	
	@Override
	public String getTitle(Locale arg0) {
		return "Reports";
	}
	
	String[] getResourceListing(String path) throws IOException, URISyntaxException {
		URL dirURL = this.getClass().getResource(path);
		return new File(dirURL.toURI()).list();			
	}
	
	@Override
	public void initUI() {
		
		table.setSizeFull();
        table.setPageLength(15);
        this.setMargin(true);
        table.addStyleName("striped");
        
        table.addContainerProperty("name", Component.class, null, ProcessbaseApplication.getString("raports", "Raport"), null, null);
        table.addContainerProperty("description", String.class, null, ProcessbaseApplication.getString("description", "Description"), null, null);
        addComponent(table);
        refreshTable();
	}
	
	public void refreshTable() {
		table.removeAllItems(); 
		try {
			File aReportDir = new File(Constants.getBonitaHomeDir(), "Reports");
			List<ReportItem> list = ReportingDataStore.getInstance().listReportsFromDirectory(aReportDir, false);
			for (ReportItem reportItem : list) {
				Item row=table.addItem(reportItem);
				
				TableLinkButton teb = new TableLinkButton(reportItem.getReportId(), "", "", reportItem, this);
				
				row.getItemProperty("name").setValue(teb);
				row.getItemProperty("description").setValue(reportItem.getDescription());
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	//open raport
	public void buttonClick(ClickEvent event) {
		
		if (event.getButton() instanceof TableLinkButton) {
            try {
            	ReportItem reportItem = (ReportItem) ((TableLinkButton) event.getButton()).getTableValue();
               	openRaportWindow(reportItem);

            } catch (Exception ex) {
                ex.printStackTrace();                
                throw new RuntimeException(ex);
            }
        }
	}
	
	private void openRaportWindow(ReportItem ri){
		log.fine("Open raport window");
		PbWindow reportWindow=new PbWindow(ri.getReportId());
		RaportViewer panel=new RaportViewer(ri);
		reportWindow.setWidth("800px");
		reportWindow.setHeight("900px");
		reportWindow.center();
		reportWindow.setContent(panel);
		panel.initUI();
		
		this.getApplication().getMainWindow().addWindow(reportWindow);
	}

}
