package com.metaenlace.actualizafirmaws.modelo;

import java.util.Date;

public class ActualizacionFirma {
	
	private byte[] firmaActualizada;
	private Date fechaDeCaducidad;
	
	
	public byte[] getFirmaActualizada() {
		return firmaActualizada;
	}
	
	public void setFirmaActualizada(byte[] firmaActualizada) {
		this.firmaActualizada = firmaActualizada;
	}
	
	public Date getFechaDeCaducidad() {
		return fechaDeCaducidad;
	}
	
	public void setFechaDeCaducidad(Date fechaDeCaducidad) {
		this.fechaDeCaducidad = fechaDeCaducidad;
	}

}
