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
package org.processbase.engine.bam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 *
 * @author mgubaidullin
 */
public class BAMConstants {

    public static boolean LOADED = false;
    public static Properties properties = new Properties();
    //public static String BAM_DB_POOLNAME;
    public static String BAM_DB_DIALECT;
    public static String BAM_MQ_CONNECTION_FACTORY;
    public static String BAM_MQ_DESTINATION_RESOURCE;

    public static void loadConstants() {
        try {
        	File file = null;
        	String userHomeDir=System.getProperty("BONITA_HOME");
            file=new File(userHomeDir+"/processbase3.properties");//global configuration can be accessed %BONITA_HOME%\processbase3.properties
            if(!file.exists())//if there is no such folder, then read embeded resource
            	file = new File("processbase3.properties");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();
                BAM_MQ_CONNECTION_FACTORY = properties.containsKey("BAM_MQ_CONNECTION_FACTORY") ? properties.getProperty("BAM_MQ_CONNECTION_FACTORY") : "jms/pbbamConnectionFactory";
                BAM_MQ_DESTINATION_RESOURCE = properties.containsKey("BAM_MQ_DESTINATION_RESOURCE") ? properties.getProperty("BAM_MQ_DESTINATION_RESOURCE") : "jms/pbbamDestinationResource";

                //BAM_DB_POOLNAME = properties.containsKey("BAM_DB_POOLNAME") ? properties.getProperty("BAM_DB_POOLNAME") : "jdbc/pbbam";
                //BAM_DB_DIALECT = properties.containsKey("BAM_DB_DIALECT") ? properties.getProperty("BAM_DB_DIALECT") : "org.hibernate.dialect.Oracle10gDialect";
                BAM_DB_DIALECT = properties.containsKey("hibernate.dialect") ? properties.getProperty("hibernate.dialect") : "org.hibernate.dialect.Oracle10gDialect";
            } else {
                properties.setProperty("BAM_MQ_CONNECTION_FACTORY", "jms/pbbamConnectionFactory");
                properties.setProperty("BAM_MQ_DESTINATION_RESOURCE", "jms/pbbamDestinationResource");
                //properties.setProperty("BAM_DB_POOLNAME", "jdbc/pbbam");
                //properties.setProperty("BAM_DB_DIALECT", "org.hibernate.dialect.Oracle10gDialect");

                FileOutputStream fos = new FileOutputStream(file);
                properties.storeToXML(fos, null);
                fos.close();
            }
            LOADED = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }
    }
/**
 * read all hibernate related properties 
 * @return
 */
	public static Properties hibernateProperties() {
		// TODO Auto-generated method stub
		if(!LOADED)
			loadConstants();
		Properties props=new Properties();
		for (Entry<Object, Object>	element : properties.entrySet()) {
			if(element.getKey().toString().matches("^hibernate*"))
				props.put(element.getKey(), element.getValue());
		}
		return props;
		
	}
}
