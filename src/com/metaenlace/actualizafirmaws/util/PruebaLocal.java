package com.metaenlace.actualizafirmaws.util;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.io.FileUtils;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaWSImpl;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;
import com.metaenlace.actualizafirmaws.modelo.Aplicacion;
import com.metaenlace.actualizafirmaws.modelo.ClienteFirma;
import com.metaenlace.actualizafirmaws.modelo.TipoFirma;

public class PruebaLocal {

	public static void main(String[] args) throws Exception {					
		byte[] ficheroEntrada = FileUtils.readFileToByteArray(new File("ruta_documento_sin_firma.pdf"));
		ActualizaFirmaWSImpl af = new ActualizaFirmaWSImpl();
		ActualizacionFirma documentoDatos = af.actualizaFirma(ficheroEntrada, TipoFirma.PADES,  Aplicacion.WEB_BORM, ClienteFirma.AFIRMA_TSA);
		
		File nuevoFile = new File("ruta_documento_dejar_con_firma.pdf");
		FileOutputStream fos = new FileOutputStream(nuevoFile);
		fos.write(documentoDatos.getFirmaActualizada());
		fos.close();		
	}	
}