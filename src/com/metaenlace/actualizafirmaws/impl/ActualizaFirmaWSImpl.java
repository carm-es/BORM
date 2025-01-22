package com.metaenlace.actualizafirmaws.impl;

import com.metaenlace.actualizafirma.propiedades.Conexion;
import com.metaenlace.actualizafirma.propiedades.Constantes;
import com.metaenlace.actualizafirma.propiedades.Utils;
import com.metaenlace.actualizafirmaws.api.ActualizaFirmaWS;
import com.metaenlace.actualizafirmaws.arangiACCV.api.ServicioACCV;
import com.metaenlace.actualizafirmaws.arangiACCV.impl.ServicioACCVImpl;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.api.ServicioCamerfirma;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.impl.ServicioCamerfirmaImpl;
import com.metaenlace.actualizafirmaws.clienteTSA.api.ServicioClienteTSA;
import com.metaenlace.actualizafirmaws.clienteTSA.impl.ServicioClienteTSAImpl;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;
import com.metaenlace.actualizafirmaws.modelo.Aplicacion;
import com.metaenlace.actualizafirmaws.modelo.ClienteFirma;
import com.metaenlace.actualizafirmaws.modelo.TipoFirma;
import com.metaenlace.actualizafirmaws.util.ActualizaFirmaUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jws.WebService;
import java.sql.Connection;

@WebService(endpointInterface = "com.metaenlace.actualizafirmaws.api.ActualizaFirmaWS")
public class ActualizaFirmaWSImpl implements ActualizaFirmaWS {
	
	private final static Logger log = LogManager.getLogger(ActualizaFirmaWSImpl.class);
	
	
	public ActualizaFirmaWSImpl() {
		super();		
	}

	@Override
	public ActualizacionFirma actualizaFirma(byte[] binarioEntrada, TipoFirma tipoFirma, Aplicacion tipoAplicacion, ClienteFirma clienteFirma) throws ActualizaFirmaException {
		Connection conexion = null;
		try {
			
			ActualizacionFirma af = null;
			switch (clienteFirma) {
				case AFIRMA:
					af = actualizaFirmaAfirma(binarioEntrada, tipoFirma, tipoAplicacion); // No contiene fecha de caducidad de sello
					break;
				case AFIRMA_TSA:
					af = actualizaFirmaTSA(binarioEntrada);
					break;
				case ACCV:
					af = actualizaFirmaACCV(binarioEntrada);				// No contiene fecha de caducidad de sello
					break;
				case CAMERFIRMA:
					af = actualizaFirmaCamerfirma(binarioEntrada);		// No contiene fecha de caducidad de sello
					break;
				default:
					af = new ActualizacionFirma(); // Vacio
			}
			log.info("Anuncio firmado, tipo --> " + clienteFirma);
			return af;
		}
		catch (ActualizaFirmaException e) {
			conexion = Conexion.obtenerConexion();
			log.error(e.getMessage());
			String body = "Error Firmando - ActualizaFirma: " + e.getMessage() ;
			String subject = "Error ActualizaFirma";
			String from = Constantes.FROM;
			String host = Constantes.HOST;
			String to = Utils.getValorConfiguracion(conexion, Constantes.BCC);
			try{
				Utils.enviarMensaje(body, host, from, to, null, null, subject);
			}catch (Exception ex) {
				log.error(ex.getMessage());
			}
			throw e;
		}finally{
			if(conexion != null){
				Conexion.cerrarConexion(conexion);
			}
		}
	}

	private ActualizacionFirma actualizaFirmaAfirma(byte[] binarioEntrada, TipoFirma tipoFirma, Aplicacion tipoAplicacion)throws ActualizaFirmaException {
		ActualizacionFirma actualizacionFirma = new ActualizacionFirma();
		actualizacionFirma.setFirmaActualizada(ActualizaFirmaUtil.actualizaFirma(binarioEntrada, tipoFirma, tipoAplicacion));
		return actualizacionFirma;
	}
	
	private ActualizacionFirma actualizaFirmaTSA(byte[] binarioEntrada) throws ActualizaFirmaException {
		ServicioClienteTSA generadorSello = new ServicioClienteTSAImpl();
		return generadorSello.sellar(binarioEntrada);
	}
	
	private ActualizacionFirma actualizaFirmaACCV(byte[] binarioEntrada) throws ActualizaFirmaException{
		ServicioACCV servicioACCV = new ServicioACCVImpl();
		return servicioACCV.sellar(binarioEntrada);
	}
	
	private ActualizacionFirma actualizaFirmaCamerfirma(byte[] binarioEntrada) throws ActualizaFirmaException{
		ServicioCamerfirma servicioCamerfirma = new ServicioCamerfirmaImpl();
		return servicioCamerfirma.sellar(binarioEntrada);
	}

}
