package com.metaenlace.actualizafirmaws.util;

import org.apache.axis.encoding.Base64;

public class ActualizadorXades extends TestClient {
   
	private byte[] binarioEntrada;
	
	public ActualizadorXades(byte[] binarioEntrada) {
		this.binarioEntrada = binarioEntrada;
	}
	
	@Override
	protected String getXmlEntrada() {				
		String paramIn = leeFichero(TestClient.rutaXMLEntradaXades);
		paramIn = paramIn.replace(TestClient.PATRON, Base64.encode(this.binarioEntrada));
		
		return paramIn;
	}          

   
}