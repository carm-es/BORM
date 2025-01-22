package com.metaenlace.actualizafirmaws.arangiCamerfirma.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;


import com.metaenlace.actualizafirma.propiedades.Constantes;
import com.metaenlace.actualizafirma.propiedades.Propiedad;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.api.ServicioCamerfirma;
import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;

import es.accv.arangi.base.certificate.validation.CAList;
import es.accv.arangi.base.document.InputStreamDocument;
import es.accv.arangi.base.signature.PAdESLTVSignature;
import es.accv.arangi.device.KeyStoreManager;

public class ServicioCamerfirmaImpl implements ServicioCamerfirma{

	
	private final static Logger log = LogManager.getLogger(ServicioCamerfirmaImpl.class);
	
	@Override
	public ActualizacionFirma sellar(byte[] binarioEntrada)	throws ActualizaFirmaException {
		
		try {
			String rutaConf = Propiedad.getRutaPropiedadCatalina() 
					+ File.separator
					+ Constantes.NOMBRE_CARPETA_PROPIEDADES
					+ File.separator;
			
			String rutaSello = rutaConf	+ Propiedad.getInstance().getProperty("actualizaFirmaWS.sello.nombre");
			String claveSello = Propiedad.getInstance().getProperty("actualizaFirmaWS.sello.password");
			String rutaCaList = rutaConf + Propiedad.getInstance().getProperty("actualizaFirmaWS.camerfirma.caList");
			
			//Obtenemos el certificado usado para la firma
			KeyStoreManager manager = new KeyStoreManager(new File(rutaSello), claveSello);
			
			//Cogemos los valores relativos a la TSA			
			String urlTsa = Constantes.TSA_URL;	
			String usuarioTsa = Constantes.TSA_USUARIO;	
			String claveTsa = Constantes.TSA_CLAVE;	
			String aliasSello = Constantes.ALIAS_SELLO;
			
			CAList caList = new CAList(new File(rutaCaList));
			caList.setValidationXML(new File(rutaCaList + File.separator + "validation_data.xml"));
			
			InputStream is = new ByteArrayInputStream(binarioEntrada);
			InputStreamDocument isd = new InputStreamDocument(is);		
			
			// -- Firma invisible
			PAdESLTVSignature signatureInv = PAdESLTVSignature.sign(
					new KeyStoreManager[] {manager},
					new String[] {aliasSello}, 
					isd, 
					new URL(urlTsa), 
					usuarioTsa, 
					claveTsa, 
					caList, 
					"Bolet�n Oficial de la Regi�n de Murcia");
			
			ActualizacionFirma actualizacionFirma = new ActualizacionFirma();
			actualizacionFirma.setFirmaActualizada(signatureInv.toByteArray());
			
			return actualizacionFirma;
			
		} catch (Exception e) {
			log.error("Actualizando mediante Camerfirma: " + e.getMessage());
			throw new ActualizaFirmaException(e.getMessage());
		}
	}

}
