package org.processbase.raports.birt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.caliburn.application.event.imp.DefaultEventAggregator;
import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.PlatformFileContext;

import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.data.engine.api.DataEngine;
import org.processbase.raports.birt.generator.ReportItem;

public class ReportService {
	BirtEngineFactory engineFactory;

	private String defaultFormat = "inline";
	private boolean svgEnabled = false;
	static Logger log = Logger.getLogger(ReportService.class.getName());
	/**
	 * Run and render a document using the given report parameters and render options
	 * @param reportItem
	 * @param parameters -  parameters to be passed to the report engine running the report
	 * @param renderOptions -  options specifying the output format
	 * @return
	 * @throws Exception
	 */
	public String runAndRender(ReportItem reportItem, Map parameters, HTMLRenderOption renderOptions) throws Exception {
        log.fine("Function: runAndRender("+reportItem.getReportName()+",  "+renderOptions+")");
        
        IReportEngine engine = BirtEngineFactory.getEngine();
        if(engine==null)
        	throw new RuntimeException("Raprot engine was not initialized ");
        
		String raportFile = reportItem.getRealPath().getAbsolutePath()+File.separator+reportItem.getFilename();
		log.info("Open raport:"+raportFile);
		if(new File(raportFile).exists()==false){
			throw new RuntimeException("Raport design file "+raportFile+" does not exist or is not accessible!");
		}
        //Open report design
        IReportRunnable design = engine.openReportDesign(raportFile);
        //create task to run and render report
        IRunAndRenderTask task = engine.createRunAndRenderTask(design);
		//task.locale=locale?:getLocale()
        int cacheSize = 100;
        //log.info "Setting memory buffer to ${cacheSize}MB"
        task.getAppContext().put(DataEngine.MEMORY_BUFFER_SIZE, cacheSize);
        // other options IN_MEMORY_CUBE_SIZE
        HashMap taskParams = getReportBirtParams(parameters, design);
        //log.debug "taskParams: ${taskParams}"
        if (taskParams != null) {
				task.setParameterValues(taskParams);
		}
        task.validateParameters();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        renderOptions.setOutputStream(buf);
        task.setRenderOption(renderOptions);
        //if(useGrailsDatasource) task.getAppContext().put("OdaJDBCDriverPassInConnection", dataSource.getConnection());
        task.run();
        String toReturn = buf.toString();
        task.close();        
        try {
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return toReturn;
    }
	
	private HashMap getReportBirtParams(Map params, IReportRunnable runnable) throws Exception {
        //log.trace "Function: getReportBirtParams(${params}, ${runnable})"
        try {
            //get parameter definitions
            // def engine = BirtEngineFactory.engine
            if (BirtEngineFactory.getEngine()==null) return null;
            IGetParameterDefinitionTask  task = BirtEngineFactory.getEngine().createGetParameterDefinitionTask(runnable);
			//task.locale=getLocale()
            Collection paramDefs = task.getParameterDefns(false);
            //iterate over each parameter definition, updating as appropriate
            //from the supplied ReportAttributes object
            HashMap paramMap = new HashMap();
            Iterator iter = paramDefs.iterator();
        	for (int i = 0; i < paramDefs.size(); i++) {
        		IParameterDefnBase it=(IParameterDefnBase) iter.next();            
                String paramName = it.getName();
                Object paramVal=null;
                if (params.containsKey(paramName)) {
                    Object p = params.get(paramName);
					switch (it.getParameterType()) {
                        case IScalarParameterDefn.TYPE_BOOLEAN:
                            paramVal = DataTypeUtil.toBoolean(p); break;
                        case IScalarParameterDefn.TYPE_DATE:
                            paramVal = DataTypeUtil.toSqlDate(p); break;
                        case IScalarParameterDefn.TYPE_TIME:
                            paramVal = DataTypeUtil.toSqlTime(p); break;
                        case IScalarParameterDefn.TYPE_DATE_TIME:
                            paramVal = DataTypeUtil.toDate(p); break;
                        case IScalarParameterDefn.TYPE_DECIMAL:
                            paramVal = DataTypeUtil.toBigDecimal(p); break;
                        case IScalarParameterDefn.TYPE_FLOAT:
                            paramVal = DataTypeUtil.toDouble(p); break;                        
                        case IScalarParameterDefn.TYPE_STRING:
                        	 paramVal = DataTypeUtil.toString(p); break;
                        case IScalarParameterDefn.TYPE_ANY:
                            paramVal = p; break;
                        case IScalarParameterDefn.TYPE_INTEGER:                        
                        	paramVal = DataTypeUtil.toInteger(p); break;
                        
                    }
                }
                if (paramVal != null) 
                	paramMap.put(paramName, paramVal);
            }
            return paramMap;
        } catch (BirtException e) {
            // log.error("BIRT Exception occured while getReportBirtParams: ${e.message}", e);
            throw new Exception(e.getMessage());
        }
    }
	
	/*private IRenderOption getRenderOption(HttpServletRequest request, String format) {
        //log.trace "Function: getRenderOption(${request}, ${format})"
        IRenderOption options = new RenderOption();
        String supportedImageFormats = "PNG;GIF;JPG;BMP";
        // set an absolute url to be used in output
        
//        if (baseURL =~ /^\w+:\/\//) { // we have a complete URL
//            options.baseURL = baseURL
//        } else if(request != null) {
//            if(generateAbsoluteBaseURL){
//                // add the protocol/host/port part of the URL
//                options.baseURL = "${request.getScheme()}://${request.getServerName()}:${request.getServerPort()}"
//                // append application context path either as absolute path or as relative
//                options.baseURL += baseURL[0] == "/" ? baseURL : request.getContextPath() + "/" + baseURL;
//            } else
//                options.baseURL = request.contextPath;
//        } else {
//            options.baseURL="/";
//        }
        //options.actionHandler = new GrailsHTMLActionHandler(options.baseURL, format?:defaultFormat)
        options.setOutputFormat(format!=null?format:"html");
        String outpuFormat=options.getOutputFormat().toLowerCase();
        
            if("html".equals(outpuFormat)){
                HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
                htmlOptions.setHtmlPagination(false);
                htmlOptions.setEmbeddable(false);
                htmlOptions.setBaseImageURL(this.getBaseImageURL());
                htmlOptions.setImageDirectory(this.getImageDir());
                htmlOptions.setSupportedImageFormats(supportedImageFormats + (svgEnabled ? ";SVG" : ""));
                return htmlOptions;
            }
            
            if("pdf".equals(outpuFormat)){
                PDFRenderOption pdfOptions = new PDFRenderOption(options);
                // pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
                pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);
                pdfOptions.setSupportedImageFormats(supportedImageFormats + (svgEnabled ? ";SVG" : ""));
                return pdfOptions;
            }
            
            return options;
        }
	*/
	private String getImageDir() {
		// TODO Auto-generated method stub
		return null;
	}
	private String getBaseImageURL() {
		// TODO Auto-generated method stub
		return null;
	}
    

}
