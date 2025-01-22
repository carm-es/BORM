package com.metaenlace.actualizafirmaws.util;

import java.util.*;
import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Configuration {
	
	private Log logger = LogFactory.getLog(Configuration.class);
	
    private static Configuration _instance = null;
    private Properties props = null;

    private Configuration() {
    	props = new Properties();
    	try {
	        InputStream in = getClass().getClassLoader().getResourceAsStream("config/actualizafirma-ws.properties");
	        if (in != null) {
	            props.load(in);
	        } else {
	        	logger.error("No se ha podido cargar el fichero de propiedades");
	        	throw new Exception("Error, no se ha podido cargar el fichero de propiedades");
	        }	
	    }
    	catch (Exception e) {
    		logger.error("Error en la carga del fichero de propiedades:" + e.getMessage());
	        e.printStackTrace();
    	}
    }

    public synchronized static Configuration getInstance() {
        if (_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }

    // Obtiene una propiedad a partir del nombre
    public String getProperty(String key) {
        String value = null;
        if (props.containsKey(key)) {
            value = (String) props.get(key);
        }        
        return value;
    }
} 
