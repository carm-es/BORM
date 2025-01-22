package com.metaenlace.actualizafirmaws.util;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.Aplicacion;
import com.metaenlace.actualizafirmaws.modelo.TipoFirma;

public class ActualizaFirmaUtil {
	
	private final static Logger log = LogManager.getLogger(ActualizaFirmaUtil.class);
	
	/**
	 * Actualiza una firma a formato longevo
	 * 
	 * @param firma a actualizar
	 * @return byte[] con el contenido de la firma a actualizar
	 * @throws Exception Cualquier error al actualizar
	 */
	public static byte[] actualizaFirma(byte[] binarioEntrada, TipoFirma tipoFirma, Aplicacion tipoAplicacion) throws ActualizaFirmaException {						
		try {
			byte[] firmaActualizada = binarioEntrada;
			String ret, mensaje;			
			switch (tipoFirma) {
			case PADES:
				ActualizadorPades actualizadorPades = new ActualizadorPades(binarioEntrada);
				ret = actualizadorPades.actualiza(tipoAplicacion);
				ParseadorXMLPades parserPades = new ParseadorXMLPades(ret);
				
				mensaje = parserPades.getMensajeResultado();
				
				if (!parserPades.hayExito()) {
					throw new ActualizaFirmaException("Excepción al actualizar: " + mensaje);							
				}										
				else {
					firmaActualizada = parserPades.getFirmaActualizada();											
				}	
				break;
			case XADES: 
				log.debug("Entra a firmar con tipo de firma XADES.");
				ActualizadorXades actualizadorXades = new ActualizadorXades(binarioEntrada);
				ret = actualizadorXades.actualiza(tipoAplicacion);
				ParseadorXMLXades parserXades = new ParseadorXMLXades(ret);
				
				mensaje = parserXades.getMensajeResultado();
				
				if (!parserXades.hayExito()) {
					throw new ActualizaFirmaException("Excepción al actualizar: " + mensaje);
				}										
				else {
					firmaActualizada = parserXades.getFirmaActualizada();											
				}
				log.debug("Sale de firmar con tipo de firma XADES.");
				break;
			case CADES: 
				ActualizadorCades actualizadorCades = new ActualizadorCades(binarioEntrada);
				ret = actualizadorCades.actualiza(tipoAplicacion);
				ParseadorXMLCades parserCades = new ParseadorXMLCades(ret);
				
				mensaje = parserCades.getMensajeResultado();
				
				if (!parserCades.hayExito()) {
					throw new ActualizaFirmaException("Excepción al actualizar: " + mensaje);					
				}										
				else {
					firmaActualizada = parserCades.getFirmaActualizada();											
				}	
				break;
			}
			return firmaActualizada;
		} catch (Exception e) {
			log.error("Actualizando mediante AFIRMA: " + e.getMessage());
			throw new ActualizaFirmaException("Excepción al actualizar: " + e.getMessage(), e);	
		}					
	}
}
