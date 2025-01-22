package com.metaenlace.actualizafirmaws.impl;

import javax.xml.ws.WebFault;

@WebFault
public class ActualizaFirmaException extends Exception {

	private static final long serialVersionUID = 1L;

	public ActualizaFirmaException() {
		super();
	}

	public ActualizaFirmaException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ActualizaFirmaException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActualizaFirmaException(String message) {
		super(message);
	}

	public ActualizaFirmaException(Throwable cause) {
		super(cause);
	}
	
	public String getFaultInfo(){
		return this.getMessage();
	}

}
