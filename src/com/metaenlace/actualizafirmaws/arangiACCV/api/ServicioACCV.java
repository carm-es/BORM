package com.metaenlace.actualizafirmaws.arangiACCV.api;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;

public interface ServicioACCV {
	
	public ActualizacionFirma sellar(byte[] binarioEntrada) throws ActualizaFirmaException;

}
