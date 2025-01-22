package com.metaenlace.actualizafirmaws.arangiCamerfirma.api;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;

public interface ServicioCamerfirma {
	
	public ActualizacionFirma sellar(byte[] binarioEntrada) throws ActualizaFirmaException;

}
