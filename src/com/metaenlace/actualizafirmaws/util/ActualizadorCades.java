package com.metaenlace.actualizafirmaws.util;

import org.apache.axis.encoding.Base64;

public class ActualizadorCades extends TestClient {
   
	private byte[] binarioEntrada;
	
	public ActualizadorCades(byte[] binarioEntrada) {
		this.binarioEntrada = binarioEntrada;
	}
	
	@Override
	protected String getXmlEntrada() {				
		String paramIn = leeFichero(TestClient.rutaXMLEntradaCades);
		paramIn = paramIn.replace(TestClient.PATRON, Base64.encode(this.binarioEntrada));
		
		return paramIn;
	}          

   
}