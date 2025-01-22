package com.metaenlace.actualizafirmaws.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.metaenlace.actualizafirma.propiedades.Constantes;
import com.metaenlace.actualizafirma.propiedades.Propiedad;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.impl.ServicioCamerfirmaImpl;
import com.metaenlace.actualizafirmaws.modelo.Aplicacion;

public abstract class TestClient {

	private final static Logger logger = LogManager.getLogger(TestClient.class);
	// Ruta donde se encuentran los ficheros de entrada a los servicios web
	protected static final String XML_ENTRADA_PREFIX = "actualizaFirmaWS.";
	protected static final String PATRON = "firma_en_base_64";
	protected static String endPoint;
	protected static String authorizationMethod = null;
	protected static String authorizationKeyStorePath = null;
	protected static String authorizationKeyStoreType = null;
	protected static String authorizationKeyStorePassword = null;
	protected static String authorizationKeyStoreCertAlias = null;
	protected static String authorizationKeyStoreCertPassword = null;
	protected static String rutaXMLEntradaPades = null;
	protected static String rutaXMLEntradaXades = null;
	protected static String rutaXMLEntradaCades = null;
	
	private static void init() throws IOException {
		endPoint = Propiedad.getInstance().getProperty("actualizaFirmaWS.endpoint");
		authorizationMethod = Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.method");
		authorizationKeyStorePath = Propiedad.getRutaPropiedadCatalina()
				+ File.separator
				+ Constantes.NOMBRE_CARPETA_PROPIEDADES
				+ File.separator
				+ Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.ks.path");
		authorizationKeyStoreType = Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.ks.type");
		authorizationKeyStorePassword = Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.ks.password");
		authorizationKeyStoreCertAlias = Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.ks.cert.alias");
		authorizationKeyStoreCertPassword = Propiedad.getInstance().getProperty("actualizaFirmaWS.authorization.ks.cert.password");
	}
	
	private static void initBORM() throws IOException {
		init();
		rutaXMLEntradaPades = Propiedad.getRutaPropiedadCatalina() + Propiedad.getInstance().getProperty("actualizaFirmaWS.ruta.xml.entrada.pades");
		rutaXMLEntradaCades = Propiedad.getRutaPropiedadCatalina() + Propiedad.getInstance().getProperty("actualizaFirmaWS.ruta.xml.entrada.cades");
		rutaXMLEntradaXades = Propiedad.getRutaPropiedadCatalina() + Propiedad.getInstance().getProperty("actualizaFirmaWS.ruta.xml.entrada.xades");
	}
	
	private static void initCalogoComercial() throws IOException {
		init();
		rutaXMLEntradaXades = Propiedad.getRutaPropiedadCatalina() + Propiedad.getInstance().getProperty("actualizaFirmaWS.ruta.xml.entrada.xades.catalogo");
	}

	private static Properties generateHandlerProperties() {
		Properties config = new Properties();
		config.setProperty("security.mode", authorizationMethod);
		config.setProperty("security.keystore.location", authorizationKeyStorePath);
		config.setProperty("security.keystore.type", authorizationKeyStoreType);
		config.setProperty("security.keystore.password", authorizationKeyStorePassword);
		config.setProperty("security.keystore.cert.alias", authorizationKeyStoreCertAlias);
		config.setProperty("security.keystore.cert.password", authorizationKeyStoreCertPassword);
		return config;
	}

	public String actualiza(Aplicacion tipoAplicacion) throws Exception {
		
		switch (tipoAplicacion) {
			case WEB_BORM:
				logger.debug("Entra a inicializar el cliente de firma, llamado desde WEB_BORM.");
				initBORM();
				logger.debug("Sale de inicializar el cliente de firma, llamado desde WEB_BORM.");
				break;
			case CATALOGO_COMERCIAL:
				initCalogoComercial();
				break;
			default:
				throw new Exception("No se han inicializado las propiedades de firma.");
		}
		
		Properties clientHandlerInitProperties = generateHandlerProperties();
		Handler reqHandler = new ClientHandler(clientHandlerInitProperties);

		// Se crea el servicio
		Service service = new Service();
		Call call = (Call) service.createCall();
		call.setTargetEndpointAddress(new java.net.URL(endPoint));
		call.setOperationName(new QName("http://soapinterop.org/", "verify"));
		call.setTimeout(new Integer(Propiedad.getInstance().getProperty("actualizaFirmaWS.timeout")));
		call.setClientHandlers(reqHandler, null);

		// Configuracion de la ruta al mensaje XML de entrada al WS
		String paramIn = getXmlEntrada();

		logger.debug("Vamos a guardar el fichero del XML de entrada.");

		String rutaFichero = Propiedad.getRutaPropiedadCatalina() + "/datos/mensajeSOAP.xml";
		FileUtils.writeStringToFile(new File(rutaFichero), paramIn);
		
		logger.debug("Fichero XML guardado");
		
		String ret = (String) call.invoke(new Object[] { paramIn });

		return ret;
	}

	// Este m�todo hay que redefinirlo en cada subclase
	protected abstract String getXmlEntrada();

	protected String leeFichero(String urlFichero) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(urlFichero));
			String leidoAux = "";
			String fichero = "";
			leidoAux = in.readLine();
			while (leidoAux != null) {
				fichero += leidoAux + "\n";
				leidoAux = in.readLine();
			}
			
			in.close();
			
			if ((fichero != null) && (fichero.trim().length() > 0)) {
				return fichero.toString();
			}
			
		} catch (Exception e) {
			logger.error("No se ha podido leer el fichero correctamente: " + e.getMessage());
		}
		
		return null;
	}
}