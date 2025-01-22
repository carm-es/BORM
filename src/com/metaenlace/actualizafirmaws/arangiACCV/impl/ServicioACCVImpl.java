package com.metaenlace.actualizafirmaws.arangiACCV.impl;

import java.io.File;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.metaenlace.actualizafirma.propiedades.Constantes;
import com.metaenlace.actualizafirma.propiedades.Propiedad;
import com.metaenlace.actualizafirmaws.arangiACCV.api.ServicioACCV;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.impl.ServicioCamerfirmaImpl;
import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;

import es.accv.arangi.base.document.ByteArrayDocument;
import es.accv.arangi.device.ACCVDeviceManager;
import es.accv.arangi.device.KeyStoreManager;
import es.accv.arangi.signature.PAdESLTVSignature;

public class ServicioACCVImpl implements ServicioACCV{
	
	private final static Logger log = LogManager.getLogger(ServicioACCVImpl.class);
	
	@Override
	public ActualizacionFirma sellar(byte[] binarioEntrada) throws ActualizaFirmaException {
		
		try {
			String rutaSello = Propiedad.getRutaPropiedadCatalina() 
					+ File.separator
					+ Constantes.NOMBRE_CARPETA_PROPIEDADES
					+ File.separator
					+ Propiedad.getInstance().getProperty("actualizaFirmaWS.sello.nombre");
			String claveSello = Propiedad.getInstance().getProperty("actualizaFirmaWS.sello.password");
			KeyStoreManager manager = new KeyStoreManager(new File(rutaSello), claveSello);
			
			ByteArrayDocument isd = new ByteArrayDocument (binarioEntrada);
			
			PAdESLTVSignature signatureInv = PAdESLTVSignature.sign (new ACCVDeviceManager[] {manager}, isd, "Bolet�n Oficial de la Regi�n de Murcia");
			ActualizacionFirma actualizacionFirma = new ActualizacionFirma();
			actualizacionFirma.setFirmaActualizada(signatureInv.toByteArray());
			
			return actualizacionFirma;
		
		} catch (Exception e) {
			log.error("Actualizando mediante ACCV: " + e.getMessage());
			throw new ActualizaFirmaException(e.getMessage());
		}
	}

}
