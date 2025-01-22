package com.metaenlace.actualizafirmaws.api;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;
import com.metaenlace.actualizafirmaws.modelo.Aplicacion;
import com.metaenlace.actualizafirmaws.modelo.ClienteFirma;
import com.metaenlace.actualizafirmaws.modelo.TipoFirma;

@WebService
public interface ActualizaFirmaWS {

	@WebMethod
	public ActualizacionFirma actualizaFirma(
			@WebParam(name = "binarioEntrada") byte[] binarioEntrada,
			@WebParam(name = "tipoFirma") TipoFirma tipoFirma,
			@WebParam(name = "tipoAplicacion") Aplicacion tipoAplicacion,
			@WebParam(name = "clienteFirma") ClienteFirma clienteFirma)
			throws ActualizaFirmaException;
	
}
