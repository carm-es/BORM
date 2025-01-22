package com.metaenlace.actualizafirmaws.clienteTSA.api;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;

public interface ServicioClienteTSA {
	
	public ActualizacionFirma sellar(byte[] ficheroFirmado) throws ActualizaFirmaException;

}
