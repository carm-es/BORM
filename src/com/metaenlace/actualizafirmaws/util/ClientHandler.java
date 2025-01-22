package com.metaenlace.actualizafirmaws.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.metaenlace.actualizafirma.propiedades.Propiedad;
import com.metaenlace.actualizafirmaws.arangiCamerfirma.impl.ServicioCamerfirmaImpl;

public class ClientHandler extends BasicHandler {
	
   private final static Logger logger = LogManager.getLogger(ClientHandler.class);
   
   /** Opci�n de seguridad UserNameToken */
   public static final String USERNAMEOPTION = WSConstants.USERNAME_TOKEN_LN;
   
   /** Opci�n de seguridad BinarySecurityToken */
   public static final String CERTIFICATEOPTION = WSConstants.BINARY_TOKEN_LN;
   
   /** Sin seguridad */
   public static final String NONEOPTION = "none";
   public static final String DIGESTPASSWORD = "DIGEST";
   public static final String TEXTPASSWORD = "TEXT";
   private static final long serialVersionUID = 1L;
   
   // Opciones de seguridad
   // Opci�n de seguridad del objeto actual
   private String securityOption = null;
   
   // Usuario para el token de seguridad UserNameToken.
   private String usernameTokenName = null;
   
   // Password para el token de seguridad UserNameToken
   private String usernameTokenPassword = null;
   
   // Tipo de password para el UserNameTokenPassword
   private String usernameTokenPasswordType = null;
   
   // Localizaci�n del keystore con certificado y clave privada de usuario
   private String keystoreLocation = null;
   
   // Tipo de keystore
   private String keystoreType = null;
   
   // Clave del keystore
   private String keystorePassword = null;
   
   // Alias del certificado usado para firmar el tag soapBody de la petici�n y que ser� alojado en el token BinarySecurityToken
   private String keystoreCertAlias = null;
   
   // Password del certificado usado para firmar el tag soapBody de la petici�n y que ser� alojado en el token BinarySecurityToken
   private String keystoreCertPassword = null;

   /**
    * Inicializa el atributo securityOption
    * @param securityOption opci�n de seguridad.
    * @throws AxisFault
    * @throws Exception
    */
   public ClientHandler(Properties config) throws AxisFault {
      if (config == null) {
         logger.warn("Fichero de configuracion de propiedades nulo");
         System.exit(-1);
      }
      try {
         securityOption = config.getProperty("security.mode").toUpperCase();
         usernameTokenName = config.getProperty("security.usertoken.user");
         usernameTokenPassword = config.getProperty("security.usertoken.password");
         usernameTokenPasswordType = config.getProperty("security.usertoken.passwordType");
         keystoreLocation = config.getProperty("security.keystore.location");
         keystoreType = config.getProperty("security.keystore.type");
         keystorePassword = config.getProperty("security.keystore.password");
         keystoreCertAlias = config.getProperty("security.keystore.cert.alias");
         keystoreCertPassword = config.getProperty("security.keystore.cert.password");
      } catch (Exception e) {
         logger.error("Error leyendo el fichero de configuraci�n de securizaci�n", e);
         System.exit(-1);
      }
      if (!securityOption.equals(USERNAMEOPTION.toUpperCase()) && !securityOption.equals(CERTIFICATEOPTION.toUpperCase()) && !securityOption.equals(NONEOPTION.toUpperCase())) {
         logger.error("Opcion de seguridad no valida: " + securityOption);
         AxisFault.makeFault(new InvalidParameterException("Opcion de seguridad no valida: " + securityOption));
      }
   }

   public void invoke(MessageContext msgContext) throws AxisFault {
	  logger.debug("Entramos en la operaci�n para llamar a @firma con el texto del XML de entrada. ");
      SOAPMessage msg,secMsg;
      Document doc = null;
      secMsg = null;
      try {
         //Obtenci�n del documento XML que representa la petici�n SOAP
         msg = msgContext.getCurrentMessage();
         
        //----------------------------------------------------------------------------------------
         logger.debug("Guardamos soap inicial. ");
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         msg.writeTo(out);
 		 String soapMesgStr = new String(out.toByteArray());
         
         String rutaFichero = Propiedad.getRutaPropiedadCatalina() + "/datos/mensajeSOAPInicial.xml";
         
         logger.debug("Guardamos soap inicial en " + rutaFichero);
 		 FileUtils.writeStringToFile(new File(rutaFichero), soapMesgStr);
 		//----------------------------------------------------------------------------------------
 		 
         doc = ((org.apache.axis.message.SOAPEnvelope) msg.getSOAPPart().getEnvelope()).getAsDocument();
         
         //Securizaci�n de la petici�n SOAP seg�n la opcion de seguridad configurada
         if (this.securityOption.equals(USERNAMEOPTION.toUpperCase())) {
        	 
            secMsg = this.createUserNameToken(doc);
            
         } else if (this.securityOption.equals(CERTIFICATEOPTION.toUpperCase())) {
        
        	logger.debug("Vamos a crear el BinarySecurityToken.");
        
            secMsg = this.createBinarySecurityToken(doc);
            
          //----------------------------------------------------------------------------------------
            logger.debug("Guardamos soap con seguridad. ");
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            secMsg.writeTo(out2);
    		String soapMesgStr2 = new String(out2.toByteArray());
            
            String rutaFichero2 = Propiedad.getRutaPropiedadCatalina() + "/datos/mensajeSOAPSecurity.xml";
            
            logger.debug("Guardamos soap con seguridad en " + rutaFichero2);
    		FileUtils.writeStringToFile(new File(rutaFichero2), soapMesgStr2);
    		 
    		logger.debug("Salimos de guardar soap con seguridad. ");
    		//----------------------------------------------------------------------------------------
    		
            
         } else {
        	 
            secMsg = msgContext.getMessage();
            
          //----------------------------------------------------------------------------------------
            logger.debug("Guardamos soap sin seguridad. ");
            ByteArrayOutputStream out3 = new ByteArrayOutputStream();
            secMsg.writeTo(out3);
    		String soapMesgStr3 = new String(out3.toByteArray());
            
            String rutaFichero3 = Propiedad.getRutaPropiedadCatalina() + "/datos/mensajeSOAPSinSeguridad.xml";
            
            logger.debug("Guardamos soap sin seguridad en " + rutaFichero3);
    		FileUtils.writeStringToFile(new File(rutaFichero3), soapMesgStr3);
    		 
    		logger.debug("Salimos de guardar soap sin seguridad. ");
    		//---------------------------------------------------------------------------------------- 
         }
         
         if (!this.securityOption.equals(NONEOPTION.toUpperCase())) {
            //Modificaci�n de la petici�n SOAP
            ((SOAPPart) msgContext.getRequestMessage().getSOAPPart()).setCurrentMessage(secMsg.getSOAPPart().getEnvelope(), SOAPPart.FORM_SOAPENVELOPE);
         }
         
      } catch (Exception e) {
    	 logger.debug("Se ha terminado mal la operación de seguridad. Lanzamos la expeción: " + e);
    	 logger.error("Invoke no válido: " + e.getMessage());
         AxisFault.makeFault(e);
      }
   }

   /**
    * Securiza, mediante el tag userNameToken, una petici�n SOAP no securizada.
    * @param soapRequest Documento xml que representa la petici�n SOAP sin securizar.
    * @return Un mensaje SOAP que contiene la petici�n SOAP de entrada securizada
    * mediante el tag userNameToken.
    * @throws TransformerConfigurationException
    * @throws TransformerException
    * @throws TransformerFactoryConfigurationError
    * @throws IOException
    * @throws SOAPException
 * @throws WSSecurityException 
    */
   private SOAPMessage createUserNameToken(Document soapEnvelopeRequest) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, IOException, SOAPException, WSSecurityException {
      ByteArrayOutputStream baos;
      Document secSOAPReqDoc;
      DOMSource source;
      Element element;
      SOAPMessage res;
      StreamResult streamResult;
      String secSOAPReq;
      WSSecUsernameToken wsSecUsernameToken;
      WSSecHeader wsSecHeader;
      
      // Inserci�n del tag wsse:Security y userNameToken
      wsSecHeader = new WSSecHeader(null, false);
      wsSecUsernameToken = new WSSecUsernameToken();
      
      if (TEXTPASSWORD.equalsIgnoreCase(usernameTokenPasswordType)) {
         wsSecUsernameToken.setPasswordType(WSConstants.PASSWORD_TEXT);
      } else if (DIGESTPASSWORD.equalsIgnoreCase(usernameTokenPasswordType)) {
         wsSecUsernameToken.setPasswordType(WSConstants.PASSWORD_DIGEST);
      } else {
         logger.warn("Tipo de password no valido: " + usernameTokenPasswordType);
         throw new SOAPException("No se ha especificado un tipo de password valido");
      }
      
      wsSecUsernameToken.setUserInfo(this.usernameTokenName, this.usernameTokenPassword);
      wsSecHeader.insertSecurityHeader(soapEnvelopeRequest);
      wsSecUsernameToken.prepare(soapEnvelopeRequest);
      
      // A�adimos una marca de tiempo inidicando la fecha de creaci�n del tag
      wsSecUsernameToken.addCreated();
      wsSecUsernameToken.addNonce();
      
      // Modificaci�n de la petici�n
      secSOAPReqDoc = wsSecUsernameToken.build(soapEnvelopeRequest, wsSecHeader);
      element = secSOAPReqDoc.getDocumentElement();
      
      // Transformaci�n del elemento DOM a String
      source = new DOMSource(element);
      baos = new ByteArrayOutputStream();
      streamResult = new StreamResult(baos);
      TransformerFactory.newInstance().newTransformer().transform(source, streamResult);
      secSOAPReq = new String(baos.toByteArray());
      
      //Creaci�n de un nuevo mensaje SOAP a partir del mensaje SOAP securizado formado
      MessageFactory mf = new org.apache.axis.soap.MessageFactoryImpl();
      res = mf.createMessage(null, new ByteArrayInputStream(secSOAPReq.getBytes()));
      
      return res;
   }

   /**
    * Securiza, mediante el tag BinarySecurityToken y firma, una petici�n SOAP no securizada.
    * @param soapEnvelopeRequest Documento xml que representa la petici�n SOAP sin securizar.
    * @return Un mensaje SOAP que contiene la petici�n SOAP de entrada securizada
    * mediante el tag BinarySecurityToken.
    * @throws TransformerFactoryConfigurationError
    * @throws TransformerException
    * @throws TransformerConfigurationException
    * @throws SOAPException
    * @throws IOException
    * @throws KeyStoreException
    * @throws CredentialException
    * @throws CertificateException
    * @throws NoSuchAlgorithmException
    */
   private SOAPMessage createBinarySecurityToken(Document soapEnvelopeRequest) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, IOException, SOAPException {
      ByteArrayOutputStream baos;
      Crypto crypto;
      Document secSOAPReqDoc;
      DOMSource source;
      Element element;
      StreamResult streamResult;
      String secSOAPReq;
      SOAPMessage res;
      WSSecSignature wsSecSignature;
      WSSecHeader wsSecHeader;
      crypto = null;
      wsSecHeader = null;
      wsSecSignature = null;
      
      //Inserci�n del tag wsse:Security y BinarySecurityToken
      wsSecHeader = new WSSecHeader(null, false);
      wsSecSignature = new WSSecSignature();
      crypto = CryptoFactory.getInstance("org.apache.ws.security.components.crypto.Merlin", this.initializateCryptoProperties());
      
      //Indicaci�n para que inserte el tag BinarySecurityToken
      wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
      
      // wsSecSignature.setUserInfo(this.usernameTokenName, this.usernameTokenPassword);
      wsSecSignature.setUserInfo(this.keystoreCertAlias, this.keystoreCertPassword);
      wsSecHeader.insertSecurityHeader(soapEnvelopeRequest);
      wsSecSignature.prepare(soapEnvelopeRequest, crypto, wsSecHeader);
      
      //Modificaci�n y firma de la petici�n
      secSOAPReqDoc = wsSecSignature.build(soapEnvelopeRequest, crypto, wsSecHeader);
      element = secSOAPReqDoc.getDocumentElement();

      //Transformaci�n del elemento DOM a String
      source = new DOMSource(element);
      baos = new ByteArrayOutputStream();
      streamResult = new StreamResult(baos);
      TransformerFactory.newInstance().newTransformer().transform(source, streamResult);
      secSOAPReq = new String(baos.toByteArray());
      
      //Creaci�n de un nuevo mensaje SOAP a partir del mensaje SOAP securizado formado
      MessageFactory mf = new org.apache.axis.soap.MessageFactoryImpl();
      res = mf.createMessage(null, new ByteArrayInputStream(secSOAPReq.getBytes()));
      return res;
   }
   

   /**
    * Establece el conjunto de propiedades con el que ser� inicializado el gestor criptogr�fico de WSS4J.
    * @return Devuelve el conjunto de propiedades con el que ser� inicializado el gestor criptogr�fico de WSS4J.
    */
   private Properties initializateCryptoProperties() {
	      Properties res = new Properties();
	      res.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
	      res.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", this.keystoreType);
	      res.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", this.keystorePassword);
	      res.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", this.keystoreCertAlias);
	      res.setProperty("org.apache.ws.security.crypto.merlin.alias.password", this.keystoreCertPassword);
	      res.setProperty("org.apache.ws.security.crypto.merlin.file", this.keystoreLocation);
	      return res;
	   }
   
}