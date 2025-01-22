package com.metaenlace.actualizafirmaws.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ParseadorXMLXades extends ParseadorXML {
	
	private static final String ERROR_TIPO_1 = "urn:oasis:names:tc:dss:1.0:resultmajor:RequesterError"; 
	private static final String ERROR_TIPO_2 = "urn:oasis:names:tc:dss:1.0:resultmajor:ResponderError";
	private static final String PENDIENTE = "urn:oasis:names:tc:dss:1.0:profiles:asynchronousprocessing:resultmajor:Pending";
	private static final String EXITO = "urn:oasis:names:tc:dss:1.0:resultmajor:Success";
    	
	public ParseadorXMLXades(String xml)
			throws ParserConfigurationException, UnsupportedEncodingException,
			SAXException, IOException {
		super(xml);		
	}
	
	public boolean pendienteDeActualizacion() {
		String resultado = getCodigoResultado();
		if (resultado.equals(ParseadorXMLXades.PENDIENTE)) {
			return true;
		}
		return false;
	}
	
	public boolean hayError() {
		String resultado = getCodigoResultado();
		if (resultado.equals(ParseadorXMLXades.ERROR_TIPO_1) || resultado.equals(ParseadorXMLXades.ERROR_TIPO_2)) {
			return true;
		}
		return false;
	}
	
	public boolean hayExito() {
		String resultado = getCodigoResultado();
		if (resultado.equals(ParseadorXMLXades.EXITO)) {
			return true;
		}
		return false;
	}
	
	public String getResponseId() {
		Node raiz = doc.getDocumentElement();
		Node optionalOutputs = getNode("dss:OptionalOutputs", raiz.getChildNodes());
		Node responseId = getNode("async:ResponseID", optionalOutputs.getChildNodes());
		String id = getNodeValue(responseId, Node.TEXT_NODE);
		return id;		
	}
	
	public Date getResponseTime() throws ParseException {
		Node raiz = doc.getDocumentElement();
		Node optionalOutputs = getNode("dss:OptionalOutputs", raiz.getChildNodes());
		Node responseTime = getNode("afxp:ResponseTime", optionalOutputs.getChildNodes());
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");		
		String fechaStr = getNodeValue(responseTime, Node.TEXT_NODE);
		Date fecha = simpleDateFormat.parse(fechaStr);
			
		return fecha;		
	}
	
	public byte[] getFirmaActualizada() throws IOException {
		Node raiz = doc.getDocumentElement();
		Node optionalOutputs = getNode("dss:OptionalOutputs", raiz.getChildNodes()); 
		Node documentWithSignature = getNode("dss:DocumentWithSignature", optionalOutputs.getChildNodes());
		Node document = getNode("dss:Document", documentWithSignature.getChildNodes());			
		Node base64XML = getNode("dss:Base64XML", document.getChildNodes());
		
		String firmaActualizadaEnBase64 = getNodeValue(base64XML, Node.CDATA_SECTION_NODE);
		byte[] firmaActualizada = Base64.decode(firmaActualizadaEnBase64);
		return firmaActualizada;
	}

}
