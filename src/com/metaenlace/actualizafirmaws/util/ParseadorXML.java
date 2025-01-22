package com.metaenlace.actualizafirmaws.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseadorXML {
	
	protected static final String EXITO = "urn:oasis:names:tc:dss:1.0:resultmajor:Success";	
	
	Document doc;

	public ParseadorXML(String xml) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		//Construimos el árbol DOM para procesar la respuesta
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF8")));	
	}
	
	public String getCodigoResultado() {
		Node raiz = doc.getDocumentElement();						
		Node nodoResultado = getNode("dss:Result", raiz.getChildNodes());		
		Node resultMajor = getNode("dss:ResultMajor", nodoResultado.getChildNodes());
		return getNodeValue(resultMajor, Node.TEXT_NODE);
	}
	
	public String getMensajeResultado() {
		Node raiz = doc.getDocumentElement();						
		Node nodoResultado = getNode("dss:Result", raiz.getChildNodes());		
		Node resultMessage = getNode("dss:ResultMessage", nodoResultado.getChildNodes());
		return getNodeValue(resultMessage, Node.TEXT_NODE);
	}
	
	//Metodos para movernos por el árbol DOM
	protected Node getNode(String tagName, NodeList nodes) {
		for (int x = 0; x<nodes.getLength(); x++) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				return node;
			}
		}
		return null;
	}
		
	protected String getNodeValue(Node node, short tipo) {
		NodeList childNodes = node.getChildNodes();
		for (int x = 0; x < childNodes.getLength(); x++) {
			Node data = childNodes.item(x);
			if (data.getNodeType() == tipo) {
				return data.getNodeValue();
			}			
		}
		return "";
	}
		
	protected String getNodeValue(String tagName, NodeList nodes, short tipo) {
		for (int x = 0; x < nodes.getLength(); x++) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				NodeList childNodes = node.getChildNodes();
				for (int y = 0; y < childNodes.getLength(); y++) {
					Node data = childNodes.item(y);
					if ( data.getNodeType() == Node.TEXT_NODE) {
						return data.getNodeValue();
					}					
				}
			}
		}
		return "";
	}

	protected String getNodeAttr(String attrName, Node node) {
		NamedNodeMap attrs = node.getAttributes();
		for (int y = 0; y < attrs.getLength(); y++ ) {
			Node attr = attrs.item(y);
			if (attr.getNodeName().equalsIgnoreCase(attrName)) {
				return attr.getNodeValue();
			}
		}
		return "";
	}

	protected String getNodeAttr(String tagName, String attrName, NodeList nodes) {
		for ( int x = 0; x < nodes.getLength(); x++) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				NodeList childNodes = node.getChildNodes();
				for (int y = 0; y < childNodes.getLength(); y++ ) {
					Node data = childNodes.item(y);
					if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
						if ( data.getNodeName().equalsIgnoreCase(attrName)) {
							return data.getNodeValue();
						}
					}
				}
			}
		}
		return "";
	}
	
	

}
