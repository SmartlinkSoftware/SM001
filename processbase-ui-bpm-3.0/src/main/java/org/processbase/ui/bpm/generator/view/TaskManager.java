package org.processbase.ui.bpm.generator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.h2.util.StringUtils;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.exception.VariableNotFoundException;
import org.ow2.bonita.util.GroovyExpression;
import org.processbase.ui.bpm.components.MultipleUploadComponent;
import org.processbase.ui.core.bonita.forms.Actions.Action;
import org.processbase.ui.core.bonita.forms.Activities.Activity;
import org.processbase.ui.core.bonita.forms.FieldValue;
import org.processbase.ui.core.bonita.forms.PageFlow;
import org.processbase.ui.core.bonita.forms.PageFlow.Pages.Page;
import org.processbase.ui.core.bonita.forms.ValuesList;
import org.processbase.ui.core.bonita.forms.VariableType;
import org.processbase.ui.core.bonita.forms.Widget;
import org.processbase.ui.core.bonita.forms.WidgetGroup;
import org.processbase.ui.core.bonita.forms.WidgetType;
import org.processbase.ui.core.bonita.process.CSSProperty;
import org.processbase.ui.core.bonita.process.ComponentStyle;
import org.processbase.ui.core.bonita.process.TableStyle;

import com.vaadin.data.Validator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class TaskManager 
{
	public static String stripGroovyExpression(String script){
		script = script.replace(GroovyExpression.START_DELIMITER, "");
		int end = script.lastIndexOf(GroovyExpression.END_DELIMITER);
		return script.substring(0, end > 0 ? end : script.length());		
	}

	private List<Action> actions;
	
	
	private Activity activity;
	
	private Set<DataFieldDefinition> activityDataFields;
	private Map<String, Object> activityVariables = new HashMap<String, Object>();
	private Widget clickedWidget;
	
	private List<Component> components=null;
	private String confirmationMessage;
	private String label;

	private Map<String, TaskField> fields;
		
	
	private final ProcessManager processManager;
	//private TaskInstance task;
	
	
	
	public TaskManager(ProcessManager processManager){
		this.processManager = processManager;		
	}
	
	/**
	 * Clean up myself
	 */
	public void Dispose() {
		
		
	}
	
	public Object evalGroovyExpression(String expression) throws Exception {
		return processManager.evalGroovyExpression(expression);
		//return GroovyUtil.evaluate(expression, processManager.getGroovyContext());
	}
	
	public Component getComponentByWidgetId(String id) throws Exception{
		TaskField taskFieldByName = findTaskFieldByName(id);
		if(taskFieldByName!=null)
			return taskFieldByName.getComponent();
		return null;
	}
	
	public TaskField findTaskFieldByName(String name){
		if(getFields().containsKey(name))
			return getFields().get(name);
		return null;
	}
	public List<Action> getActions() {
		return actions;
	}
	
	
	public Map<String, Object> getActivityVariables() {
		//updateActionValues();
		return activityVariables;
	}
	
	public List<Component> getComponents() {
		return components;
	}
	public String getConfirmationMessage() {
		if(StringUtils.isNullOrEmpty(confirmationMessage))
			return null;
		
		if(GroovyExpression.isGroovyExpression(confirmationMessage))
		{
			try {
				Object result=evalGroovyExpression(confirmationMessage);
				if(result!=null)
					return result.toString();
			} catch (Exception e) {
				return null;
			}
		}
		
		return confirmationMessage;
	}	
	
	public Map<String, TaskField> getFields() {
		if(fields==null)
			fields=new Hashtable<String, TaskField>();
		return fields;
	}
	public Iterable<TaskField> getIterator() {
        return fields.values();
    }
	
	public TaskField getWidgetField(Widget widget) {
		return getFields().get(widget.getId());
	}
	
	public Object getWidgetValue(Widget widget) throws Exception {
		if (widget.getInitialValue() != null && widget.getInitialValue().getExpression() != null) {				
			return evalGroovyExpression(widget.getInitialValue().getExpression());			
		}
		if (widget.getAvailableValues() != null) {
			if (widget.getAvailableValues().getExpression() != null) {
				return evalGroovyExpression(widget.getAvailableValues().getExpression());
				
			} else if (!widget.getAvailableValues().getValuesList().getAvailableValues().isEmpty()) {
				List<String>options = new ArrayList<String>();
				for (ValuesList.AvailableValue avalue : widget.getAvailableValues().getValuesList().getAvailableValues()) {
					options.add(avalue.getValue());
				}
				return options;
			}
		}
		return null;
	}
	
	
	public void onFinishTask() throws Exception {
		
		//validate all fields in task
		List<String> errors=new ArrayList<String>();
		
		//save all data from fields
		for (Entry<String, TaskField> field : getFields().entrySet()) {
			TaskField taskField = field.getValue();
			
			if(taskField.isReadOnly())//skip readonly components
				continue;
			
			String error=taskField.validate();
			if(error!=null)
				errors.add(error);
			if(taskField.getActions()!=null){
				for (Action action : taskField.getActions()) {
					if(action.getVariableType()!=null){
						if(action.getVariableType().equals(VariableType.PROCESS_VARIABLE)){							
							processManager.registerModifiedVariable(action.getVariable());
							processManager.updateVariableValue(action.getVariable(), taskField.getComponentValue(action));
						}
						else if(action.getVariableType().equals(VariableType.ACTIVITY_VARIABLE)){
							processManager.registerModifiedVariable(action.getVariable());
							updateVariableValue(action.getVariable(), taskField.getComponentValue(action));
						}
					}					
				}
			}
		}
		if(errors.size()>0)
			return; //do not continue
			//throw new Exception("Validation errors"+errors.toString());
		
		if(processManager.getTaskInstance()==null) {//this is a
			 try {
				processManager.initializeNewProcess();
				processManager.finishTask(this);
				
			} catch (ProcessNotFoundException e) {
				e.printStackTrace();
			} catch (VariableNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		else{
			processManager.finishTask(this);
		}
	}
	
	public void onTaskFieldButtonClick(TaskField field, ClickEvent event) throws Exception{
		if(field.getWidget().getType()==WidgetType.BUTTON_SUBMIT){
			if(actions!=null){
				for (Action action : actions) {
					if(action.getSubmitButton()!=null && action.getSubmitButton().equalsIgnoreCase(field.getName())){
						field.addAction(action);
					}	
				}
			}			
			try{
				onFinishTask();
			}catch(Validator.InvalidValueException e){
				getProcessManager().getWindow().showError(e.getMessage());
			}
		}		
	}	
	
	
	
	public void prepareProcessVariables() {
		try {
            if (this.processManager.getTaskInstance() != null) {
            	processManager.setProcessDataFieldDefinitions(processManager.getBpmModule().getProcessDataFields(processManager.getTaskInstance().getProcessDefinitionUUID()));
                setActivityDataFieldDefinitions(processManager.getBpmModule().getActivityDataFields(processManager.getTaskInstance().getActivityDefinitionUUID()));
                
                processManager.setProcessVariables(processManager.getBpmModule().getProcessInstanceVariables(processManager.getTaskInstance().getProcessInstanceUUID()));
                setActivityVariables(processManager.getBpmModule().getActivityInstanceVariables(processManager.getTaskInstance().getUUID()));
                activityVariables.putAll(processManager.getProcessVariables());
            } 
        } catch (Exception ex) {
            ex.printStackTrace();
        }		
	}
	
	public void registerComponent(Component component, String name) {
		TaskField field=findTaskFieldByName(name);
		if(field!=null){
			field.setComponent(component);
		}		
	}

	public Component RenderDynamicPageComponents(String pageLabel) throws Exception {
		setLabel(pageLabel);
		VerticalLayout layout=new VerticalLayout(); 
		for (DataFieldDefinition dataField : processManager.getProcessDataFields()) {
			
			Widget w = new Widget();
			w.setType(WidgetType.TEXT);
			
			w.setId(dataField.getName());
			w.setReadonly(true);
			w.setAllowHtmlInField(false);
			w.setVariableBound("${" + dataField.getName() + "}");
			w.setLabel(dataField.getLabel());

			FieldValue value = new FieldValue();
			value.setExpression(w.getVariableBound());

			w.setInitialValue(value);
			TaskField field=new TaskField(this, w);
			layout.addComponent(field.getComponent());
			
		}

		// append last submit button
		Widget submitWidget = new Widget();
		submitWidget.setType(WidgetType.BUTTON_SUBMIT);
		submitWidget.setId("submit");
		submitWidget.setLabelButton(false);
		submitWidget.setLabel("Submit");
		
		TaskField field=new TaskField(this, submitWidget);
		layout.addComponent(field.getComponent());
		
		return layout;
		
	}
	
	private Component RenderPageComponents(Page page) throws Exception {
	
		setLabel(page.getPageLabel());
		int gridColumns=1;
		int gridRows=page.getWidgets().getWidgetsAndGroups().size();
		if(page.getActions()!=null) 
			this.setActions(page.getActions().getActions());
		else 
			this.setActions(null);
		TableStyle tableStyle = processManager.getBarResource().getTableStyle(page);
		if(tableStyle!=null)
		{
			gridColumns=tableStyle.getColumns();
			gridRows=tableStyle.getRows();
		}
		
		GridLayout gridLayout = new GridLayout(gridColumns, gridRows);
		gridLayout.setSizeFull();
		gridLayout.setSpacing(true);
		int row1 = 0;
		int row2 = 0;
		
		for (Object wg : page.getWidgets().getWidgetsAndGroups()) {
			Component c = null;
			if (wg instanceof Widget) {
				Widget widget = (Widget) wg;
				
				if(widget.getDisplayCondition() != null){
					
					Object condition = widget.getDisplayCondition();
					if (GroovyExpression.isGroovyExpression(widget.getDisplayCondition())) {
						try {
							condition = evalGroovyExpression(widget.getDisplayCondition());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					boolean show = false;
					try {
						show = Boolean.parseBoolean(condition.toString());
					} catch (Exception e) {
					}
					
					if(!show){
						continue;
					}
					
				}
					
				TaskField field=new TaskField(this, widget);
				
				c = field.getComponent();
				
				//remove submit buttons from grid and later put them into button bar
				if(field.getWidget().getType()==WidgetType.BUTTON_SUBMIT)
				{
					c=new Label();
					((Label)c).setVisible(false);
				}
				
				if (c != null) {
					//let's check if this widget is actually a multiple attachment component 
					if(widget.getType()==WidgetType.LISTBOX_MULTIPLE){
						
						String processVariableDataType=getProcessManager().getProcessDataTypeByVariableName(field.getVariableBound());
						//DataFieldDefinition processDataField = getProcessManager().getBpmModule().getProcessDataField(getProcessManager().processDefinitionUUID, widget.getVariableBound());
						
						if("Attachment".equalsIgnoreCase(processVariableDataType) || field.getName().startsWith("attachment") ){ //we must render a upload components
							c=new MultipleUploadComponent(field, getProcessManager().getProcessInstanceUUID(), widget);
							field.setReadOnly(true);
						}
					}
					int col1 = 0;
					int col2 = 0;
					if (tableStyle!=null) {
						ComponentStyle componentStyle = tableStyle.getElements().get(widget.getId());
						
						col1 = componentStyle.getPosition().getFColumn();
						row1 = componentStyle.getPosition().getFRow();
						col2 = componentStyle.getPosition().getTColumn();
						row2 = componentStyle.getPosition().getTRow();
						
						// --- #1660 ---
						//Check that cell is actually empty, if not move element to right
						for (int i = row1; i <= row2 ; i++) {
							for (int j = col2; j <= col2; j++) {
								Component tmp = gridLayout.getComponent(j, i);
								if(tmp != null){
									col1++;
									col2++;
								}
							}
						}
						// --- #1659 ---
						CSSProperty css = componentStyle.getCss();
						if(css != null){
							if(css.getWidth() != null && css.getWidth().getSize() != null){
								c.setWidth(css.getWidth().getSize(), css.getWidth().getSizableUnit());
							}
							if(css.getHeigth() != null && css.getHeigth().getSize() != null){
								c.setHeight(css.getHeigth().getSize(), css.getHeigth().getSizableUnit());
							}
						}
						//-------
						gridLayout.addComponent(c, col1, row1, col2, row2);
					} else {
						gridLayout.addComponent(c, col1, row1, col2, row2);
						row2++;
						row1++;
					}
				}
			} else if (wg instanceof WidgetGroup) {
				// TODO WidgetGroup
			}
		}
		gridLayout.setCaption(getLabel());
		return gridLayout;
		
	}
	public List<Component> renderPageflow(PageFlow pageFlow) throws Exception {
		processManager.initProcessVariables();
		components=new ArrayList<Component>();
		if(pageFlow==null){
			setLabel(processManager.getTaskInstance().getActivityName());
			components.add(RenderDynamicPageComponents(processManager.getTaskInstance().getActivityLabel()));
			return components;
		}
		confirmationMessage=pageFlow.getConfirmationMessage();
		
		
		List<Page> pages = pageFlow.getPages().getPages();
		components=new ArrayList<Component>(pages.size());
		for (Page page : pages) {
			components.add(RenderPageComponents(page));
		}
		return components;
	}
	
	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;	
		
		
	}
	public void setActivityDataFieldDefinitions(
			Set<DataFieldDefinition> activityDataFields) {
				this.activityDataFields = activityDataFields;
		
	}
	public void setActivityVariables(Map<String, Object> activityVariables) {
		this.activityVariables = activityVariables;
	}
	
	public void setComponents(List<Component> components) {
		this.components = components;
	}
	
	public void setFields(Map<String, TaskField> fields) {
		this.fields = fields;
	}
	
		
	public void updateVariableValue(String variable, Object componentValue) {
		activityVariables.put(variable, componentValue);		
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public ProcessManager getProcessManager() {
		return processManager;
	}

	public void showInformation(String msg){
		
	}
	
}
