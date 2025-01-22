package com.metaenlace.actualizafirmaws.util;

import org.apache.axis.encoding.Base64;

public class ActualizadorPades extends TestClient {
   
	private byte[] binarioEntrada;
	
	public ActualizadorPades(byte[] binarioEntrada) {
		this.binarioEntrada = binarioEntrada;
	}
	
	@Override
	protected String getXmlEntrada() {				
		String paramIn = leeFichero(TestClient.rutaXMLEntradaPades);
		paramIn = paramIn.replace(TestClient.PATRON, Base64.encode(this.binarioEntrada));
		
		return paramIn;
	}          

   
}