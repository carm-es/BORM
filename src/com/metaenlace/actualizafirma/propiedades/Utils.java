package com.metaenlace.actualizafirma.propiedades;

import com.metaenlace.actualizafirmaws.impl.ActualizaFirmaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;

public class Utils {
	
	public static void enviarMensaje(String mensaje, String host, String from, String to, String cc, String bcc, String subject) throws Exception {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", host);
			
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			StringTokenizer st = new StringTokenizer(to,",");
			while (st.hasMoreTokens()) {
				String direccion = (String) st.nextToken();
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(direccion));
			}

			if (cc != null) {
				st = new StringTokenizer(cc, ",");
				while (st.hasMoreTokens()) {
					String direccion = (String) st.nextToken();
					message.addRecipient(Message.RecipientType.CC, new InternetAddress(direccion));
				}
			}
			
			if (bcc != null) {
				st = new StringTokenizer(bcc, ",");
				while (st.hasMoreTokens()) {
					String direccion = (String) st.nextToken();
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(direccion));
				}
			}
			
			message.setSubject(subject,StandardCharsets.UTF_8.name());

			Multipart mp = new MimeMultipart();

			//Primera parte del mensaje						
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setContent(mensaje, "text/plain; charset=UTF-8");
	        mp.addBodyPart(mbp);

	        message.setContent(mp);
			Transport.send(message);
			
		} catch (Exception e) {
			throw e;
		}
		
	}

	private static final Logger logger = LogManager.getLogger(Utils.class);

	public static String getValorConfiguracion(Connection con, String nombre) throws ActualizaFirmaException {
		try {
			String sql = "SELECT VALOR FROM CONFIGURACION WHERE PARAMETRO = '" + nombre + "'";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getString("VALOR");
			}
		}
		catch (Exception e) {
			logger.error("Error conector BD externa: "+ e.getMessage());
			throw new ActualizaFirmaException("Error al conectar con BD Externa. Pongase en contacto con el BORM. "+ e.getMessage());
//			throw new ServicioException("Error enviando al BOE. Pongase en contacto con el BORM. "+ e.getMessage());
		}
		return null;
	}
	
}
