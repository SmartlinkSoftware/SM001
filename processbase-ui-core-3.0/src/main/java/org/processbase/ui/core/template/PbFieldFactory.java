/**
 * Copyright (C) 2010 PROCESSBASE Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.processbase.ui.core.template;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import java.util.Date;
import java.util.ResourceBundle;

/**
 *
 * @author mgubaidullin
 */
public class PbFieldFactory implements FormFieldFactory {
    protected ResourceBundle messages = null;

    public PbFieldFactory(ResourceBundle messages) {
        super();
        this.messages = messages;
    }

    public Field createField(Class type, Component uiContext) {
        return null;
    }

    public Field createField(Property property, Component uiContext) {
        return null;
    }

    public Field createField(Item item, Object propertyId, Component uiContext) {
        String pid = (String) propertyId;
        if (pid.equals("sectionName") ){
           TextField tf = new TextField(messages.getString("sectionName"));
            tf.setNullRepresentation("");
            tf.setWidth("300px");
            tf.setRequired(true);
            tf.setRequiredError(messages.getString("requiredField"));
            return tf;
        } else if (pid.equals("sectionDesc") ){
           TextField tf = new TextField(messages.getString("sectionDesc"));
            tf.setNullRepresentation("");
            tf.setWidth("150px");
            tf.setRequired(true);
            tf.setRequiredError(messages.getString("requiredField"));
            return tf;
        } else if (pid.equals("taskName") || pid.equals("name")) {
            TextField tf = new TextField(("taskName"));
            tf.setNullRepresentation("");
            tf.setSizeFull();
            tf.setRequired(true);
            tf.setRequiredError("Обязательное поле!");
            return tf;
        } else if (pid.equals("dataXml")) {
            RichTextArea ra = new RichTextArea();
            ra.setCaption(("taskText"));
            ra.setNullRepresentation("");
            ra.setRequired(true);
            ra.setRequiredError("Обязательное поле!");
            return ra;
        } else if (pid.equals("startDate") || pid.equals("expireDate") || pid.equals("endDate")) {
            PopupDateField df = new PopupDateField((pid));
            df.setResolution(DateField.RESOLUTION_MIN);
            df.setValue(new Date());
            df.setRequired(true);
            df.setRequiredError("Обязательное поле!");
            return df;
        } else if (pid.equals("nxuserId")) {
            TextField tf = new TextField(("author"));
            tf.setNullRepresentation("");
            tf.setSizeFull();
            tf.setRequired(true);
            tf.setReadOnly(true);
            return tf;
        } else if (pid.equals("assignedTo")) {
            TextField tf = new TextField(("assignedTo"));
            tf.setNullRepresentation("");
            tf.setSizeFull();
            tf.setRequired(true);
            tf.setReadOnly(true);
            return tf;
        } else if (pid.equals("status")) {
            TextField tf = new TextField(("status"));
            tf.setNullRepresentation("");
            tf.setReadOnly(true);
            return tf;
        } else if (pid.equals("id")) {
            TextField tf = new TextField("ID");
            tf.setNullRepresentation("");
            tf.setReadOnly(true);
            return tf;
        }
        return null;
    }

    public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
        return null;
    }
}
