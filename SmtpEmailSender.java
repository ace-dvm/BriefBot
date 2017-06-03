package bbtrial.nl.logicgate.ace;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

	/**
	 * 
	 * depends upon http://java.sun.com/products/javamail/
	 * 
	 * http://java.sun.com/products/javamail/downloads/index.html
	 * 
	 * @author eric
	 * 
	 */

	public class SmtpEmailSender implements EmailSender {

	    public static final int DEFAULT_SMTP_PORT = 25;

	    private String smtpServer;

	    private int port;

	    private String smtpAccount;

	    private String smtpPassword;

	    private boolean enableTLS;

	    private boolean enableDebug;

	    private String defaultFrom;

	    public SmtpEmailSender(String smtpServer, int smtpPort, String smtpAccount,
	        String smtpPassword, boolean enableTLS, String defaultFrom) {

	        if (smtpServer == null || smtpServer.equals("")) {
	            this.smtpServer = "localhost";
	        } else {
	            this.smtpServer = smtpServer;
	        }

	        if (smtpPort <= 0) {
	            this.port = DEFAULT_SMTP_PORT;
	        } else {
	            this.port = smtpPort;
	        }

	        this.smtpAccount = smtpAccount;
	        this.smtpPassword = smtpPassword;
	        this.enableTLS = enableTLS;
	        this.enableDebug = false;
	        this.defaultFrom = defaultFrom;
	    }

	    public void setEnableDebug(boolean enableDebug) {
	        this.enableDebug = enableDebug;
	    }

	    @Override
		public void sendMail(String to, String subject, String body)
				throws AddressException, MessagingException {

	        sendMail(defaultFrom, to, subject, body, null,
	            new HashMap<String, String>());
	    }

	    @Override
	    public void sendMail(String from, String to, String subject,
	        String textBody, String htmlBody, Map<String, String> headers)
	        throws AddressException, MessagingException {

	        Session mailSession = createMailSession();

	        MimeMessage msg = new MimeMessage(mailSession);
	        msg.setFrom(new InternetAddress(from));
	        msg.setRecipients(Message.RecipientType.TO, to);
	        msg.setSubject(mimeEncode(subject));
	        msg.setSentDate(new Date());

	        addHeaders(msg, headers);

	        Multipart mp = createAndFillMultiPart(textBody, htmlBody);

	        msg.setContent(mp);
	        msg.saveChanges();

	        Transport.send(msg);
	    }

	    private Session createMailSession() {
	        Properties mailProps = createMailSessionProperties();

	        Session mailSession = getMailSession(mailProps);
	        mailSession.setDebug(enableDebug);

	        return mailSession;
	    }

	    private void addHeaders(MimeMessage msg, Map<String, String> headers)
	        throws MessagingException {

	        // Add any "required" headers ...
	        msg.addHeader("X-Eric-Conspiracy", "There is no conspiracy");

	        // even custom headers passed in by the caller
	        for (Map.Entry<String, String> mapEntry : headers.entrySet()) {
	            String header = mapEntry.getKey();
	            String value = mimeEncode(mapEntry.getValue());
	            msg.addHeader(header, value);
	        }
	    }

	    private Multipart createAndFillMultiPart(String textBody, String htmlBody)
	        throws MessagingException {

	        Multipart mp = new MimeMultipart("alternative");

	        BodyPart bpText = new MimeBodyPart();
	        bpText.setContent(textBody, "text/plain;charset=UTF-8");
	        mp.addBodyPart(bpText);

	        if (htmlBody != null) {
	            BodyPart bpHtml = new MimeBodyPart();
	            bpHtml.setContent(htmlBody, "text/html;charset=UTF-8");
	            mp.addBodyPart(bpHtml);
	        }

	        return mp;
	    }

	    private Properties createMailSessionProperties() {
	        Properties mailProps = new Properties();
	        if (enableDebug) {
	            mailProps.put("mail.debug", "" + enableDebug);
	        }
	        mailProps.put("mail.smtp.host", smtpServer);
	        mailProps.put("mail.smtp.port", "" + port);

	        /*
	         * allow 8-bit mime, which the SMTP protocol says it will STILL fall
	         * back to 7bit (base64) encoding if the server doesn't support it.
	         * We've had good luck with well-configured servers before...
	         */
	        mailProps.put("mail.smtp.allow8bitmime", true);

	        /*
	         * It's usually a good idea to set TLS as true, because if the server
	         * does not support it, it should at least fall back to non TLS.
	         */
	        mailProps.put("mail.smtp.starttls.enable", "" + enableTLS);

	        return mailProps;
	    }

	    private Session getMailSession(Properties mailProps) {
	        // if no smtp account is specified, try to get a generic session.
	        if (smtpAccount == null || smtpAccount.length() == 0) {
	            return Session.getInstance(mailProps);
	        }

	        // else an smtp account is specified, so turn on authentication.
	        mailProps.put("mail.smtp.auth", Boolean.TRUE.toString());

	        Authenticator authenticator = new Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(smtpAccount, smtpPassword);
	            }
	        };

	        // and get a session using simple PasswordAuthentication
	        return Session.getInstance(mailProps, authenticator);
	    }

	    private String mimeEncode(String value) {
	        try {
	            return MimeUtility.encodeText(value, "UTF-8", null);
	        } catch (UnsupportedEncodingException e) {
	            throw new RuntimeException(e.getMessage() + " (" + value + ")", e);
	        }
	    }

	    /* really, only useful for debug */
	    public String toString() {
	        StringBuffer buf = new StringBuffer();
	        buf.append(getClass().getSimpleName());
	        buf.append(" [smtpServer: ").append(smtpServer);
	        buf.append(", port: ").append(port);
	        buf.append(", smtpAccount: ").append(smtpAccount);
	        // buf.append(", smtpPassword: ").append(smtpPassword);
	        buf.append(", enableTLS: ").append(enableTLS);
	        buf.append(", enableDebug: ").append(enableDebug);
	        buf.append("]");
	        return buf.toString();
	    }


}
