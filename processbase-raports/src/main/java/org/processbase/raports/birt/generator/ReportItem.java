package org.processbase.raports.birt.generator;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.console.client.BonitaUUID;
import org.bonitasoft.console.client.Item;
import org.bonitasoft.console.client.reporting.ReportScope;
import org.eclipse.birt.report.engine.api.IReportDocumentInfo;
import org.processbase.ui.core.Constants;

public class ReportItem implements Item{

	private IReportDocumentInfo docInfo;
    private int lastCheckpoint;
    private String viewerID;
    private boolean renderComplete;
    private String reportName = "";
	private final String reportId;
	public String getReportId() {
		return reportId;
	}
	public String getFilename() {
		return filename;
	}
	public String getDescription() {
		return description;
	}
	public String getReportType() {
		return reportType;
	}
	public ReportScope getScope() {
		return scope;
	}
	public boolean isEditable() {
		return editable;
	}
	private final String filename;
	private final String description;
	private final String reportType;
	private final ReportScope scope;
	private final boolean editable;
	private Set<String> myConfigurationElements;
	private File reportPath;
    
    public ReportItem(String reportId, String theFilename, String theDescription,
			String theReportType, ReportScope theReportScope, boolean editable) {
				this.reportId = reportId;
		// TODO Auto-generated constructor stub
				this.filename = theFilename;
				this.description = theDescription;
				this.reportType = theReportType;
				this.scope = theReportScope;
				this.editable = editable;
				myConfigurationElements= new HashSet<String>();
	}
	public String getReportName() {
            return reportName;
    }
    public void setReportName(String reportName) {
            this.reportName = reportName;
    }
    public boolean isRenderComplete() {
            return renderComplete;
    }
    public void setRenderComplete(boolean renderComplete) {
            this.renderComplete = renderComplete;
    }
    public String getViewerID() {
            return viewerID;
    }
    public void setViewerID(String viewerID) {
            this.viewerID = viewerID;
    }
    public IReportDocumentInfo getDocInfo() {
            return docInfo;
    }
    public void setDocInfo(IReportDocumentInfo docInfo) {
            this.docInfo = docInfo;
    }
    public int getLastCheckpoint() {
            return lastCheckpoint;
    }
    public void setLastCheckpoint(int lastCheckpoint) {
            this.lastCheckpoint = lastCheckpoint;
    }
    public Set<String> getConfigurationElements() {
        final HashSet<String> theResult = new HashSet<String>();
        theResult.addAll(myConfigurationElements);
        return theResult;
    }
    
    public void setConfigurationElements(Set<String> theNewConfiguration) {
        myConfigurationElements.clear();
        if(theNewConfiguration!=null && !theNewConfiguration.isEmpty()) {
            myConfigurationElements.addAll(theNewConfiguration);
        }
    }
	public File getRealPath() {
		return reportPath;
	}
	public void setReportPath(File folder) {
		reportPath=folder;
		
	}
	public BonitaUUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}
	public void updateItem(Item aSource) {
		// TODO Auto-generated method stub
		
	}


}
