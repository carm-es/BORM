package com.metaenlace.actualizafirmaws.clienteTSA.impl;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignature;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.metaenlace.actualizafirmaws.clienteTSA.api.ServicioClienteTSA;
import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import com.metaenlace.actualizafirmaws.modelo.ActualizacionFirma;
import com.steria.tsa.cliente.GeneradorTS;
import com.steria.tsa.cliente.respuestas.RespuestaGenerarTS;
import com.steria.tsa.cliente.tipos.TipoLlamada;
import com.steria.tsa.cliente.tipos.TiposInputDocument;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

public class ServicioClienteTSAImpl implements ServicioClienteTSA{
	
	private final static Logger log = LogManager.getLogger(ServicioClienteTSAImpl.class);
	
	
	@Override
	public ActualizacionFirma sellar(byte[] ficheroFirmado) throws ActualizaFirmaException {
		
		FileOutputStream fos = null;
		File ficheroResultado = null;
		
		try{
			ficheroResultado = getFileTemp();
			
			InputStream myInputStream = new ByteArrayInputStream(ficheroFirmado); 
			
			PdfReader reader = new PdfReader(myInputStream);
	
			AcroFields af = reader.getAcroFields();
			ArrayList<String> names = af.getSignatureNames();
		
			fos = new FileOutputStream(ficheroResultado);
			PdfStamper stp;
			if (names == null || names.isEmpty()) {
				stp = PdfStamper.createSignature(reader, fos, '\0');
			} else {
				stp = PdfStamper.createSignature(reader, fos, '\0', null, true);
			}
			PdfSignatureAppearance sap = stp.getSignatureAppearance();

			PdfSignature timeStampSignature = new PdfSignature(PdfName.ADOBE_PPKLITE, new PdfName("ETSI.RFC3161"));
			timeStampSignature.put(PdfName.TYPE, new PdfName("DocTimeStamp"));
			sap.setCryptoDictionary(timeStampSignature);

			int csize = 15000;
			HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
			exc.put(PdfName.CONTENTS, new Integer(csize * 2 + 2));
			sap.preClose(exc);

			// Cogemos el contenido de la firma y lo mandamos a sellar
			byte[] content = IOUtils.toByteArray(sap.getRangeStream());

			// Generamos un nuevo sello para los datos
			byte[] timestampToken = generaSello(content);
			
			// Construimos un objeto de TimeStampToken de bouncycastle
			CMSSignedData cmsSD = new CMSSignedData(Base64.decode(timestampToken));
			TimeStampToken tstoken = new TimeStampToken(cmsSD);
			
			// EXTRAER FECHA DE CADUCIDAD DEL SELLO QUE INTRODUCIMOS 
			Date fechaCaducidad = extraerFechaCaducidadSello(tstoken);
			
			// Lo insertamos en el sitio correspondiente
			byte[] outc = new byte[csize];
			PdfDictionary dic = new PdfDictionary();
			System.arraycopy(tstoken.getEncoded(), 0, outc, 0, tstoken.getEncoded().length);
			dic.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));

			sap.close(dic);
			
			// Devolvemos un tipo de dato nuestro con la firma actualizada y la caducidad del sello
			ActualizacionFirma actualizacionFirma = new ActualizacionFirma();
			actualizacionFirma.setFirmaActualizada(FileUtils.readFileToByteArray(ficheroResultado));
			actualizacionFirma.setFechaDeCaducidad(fechaCaducidad);
			
			return actualizacionFirma;

		} catch (Exception e) {
			log.error("Actualizando mediante TSA: " + e.getMessage());
			ficheroResultado.delete();
			throw new ActualizaFirmaException("Excepcion al actualizar: " + e.getMessage(), e);	
		}finally {
			if (fos != null) {
				try {
					fos.close();
					
				} catch (IOException e) {
					log.error("Cerrando el FileOutputStream: " + e.getMessage());
					throw new ActualizaFirmaException("Excepci�n al cerrar stream: " + e.getMessage(), e);
				}
			ficheroResultado.delete();	
			}
		}
	}

	private Date extraerFechaCaducidadSello(TimeStampToken tstoken) {
		Store certs = tstoken.getCertificates(); 
		ArrayList<X509CertificateHolder> listCert = new ArrayList(certs.getMatches(null));
		X509CertificateHolder cert = listCert.get(0);
		Date fechaCaducidad = cert.getNotAfter();
		
		return fechaCaducidad;
	}

	private byte[] generaSello(byte[] datos) {
		GeneradorTS generador = new GeneradorTS();
		RespuestaGenerarTS respuesta = generador.generarTS(TipoLlamada.RFC3161,
				datos, TiposInputDocument.DOCUMENT_HASH, "id-borm", false);
		
		return respuesta.getTimeStamp();
	}
	
	private File getFileTemp() throws IOException {
		File temp = File.createTempFile("selladoTemporal", ".pdf");
		//temp.deleteOnExit();
		return temp;
	}
	

	
}
