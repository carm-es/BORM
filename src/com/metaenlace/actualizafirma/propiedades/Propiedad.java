package com.metaenlace.actualizafirma.propiedades;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;



public class Propiedad {
	
	private final static Logger log = LogManager.getLogger(Propiedad.class);
	
	private static final String RUTA_PROPIEDADES = Constantes.NOMBRE_CARPETA_PROPIEDADES
					+ File.separator
					+ Constantes.NOMBRE_FICHERO_PROPIEDAES;
    private static Properties props;
    private static Propiedad instance;
    private static String rutaPropiedadCatalina = null;
 
    private Propiedad() throws IOException{
        cargarPropiedades();
    }
 
    public static Propiedad getInstance() throws IOException{
        if(instance == null){
            instance = new Propiedad();
        }
        return instance;
    }
 
    private static void cargarPropiedades() throws IOException{
        props = new Properties();
        try{
			rutaPropiedadCatalina = getRutaPropiedadCatalina();
			String rutaCompleta = rutaPropiedadCatalina + File.separator + RUTA_PROPIEDADES;
			FileInputStream fis = new FileInputStream(new File(rutaCompleta));
			props.load(fis);
        }
        catch (IOException e){
        	log.error("No se ha cargado bien el fichero de propiedades." + e.getMessage());
        	throw e;
        }
    }
 
    public String getProperty(String nombrePropiedad) {
        String value = null;
        if (props.containsKey(nombrePropiedad)){
            value = (String) props.get(nombrePropiedad);
        }
        else{
        	log.error("Propiedad ["+ nombrePropiedad +"] no encontrada");
        }
        return value;
    }
    
    public static String getRutaPropiedadCatalina() {
    	if(rutaPropiedadCatalina == null)
    		return System.getProperty("catalina.home");
    	
		return rutaPropiedadCatalina;
	}
	
}
