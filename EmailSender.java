package bbtrial.nl.logicgate.ace;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public interface EmailSender {

	void sendMail(String to, String subject, String body) //
	throws AddressException, MessagingException;

	void sendMail(String from, String to, String subject, String textBody,
			String htmlBody, Map<String, String> extraHeaders) //
	throws AddressException, MessagingException;
	
}
