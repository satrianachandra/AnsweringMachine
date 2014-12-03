/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
 
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


public class SendMailTLS {
 
    
        public void sendmail(String receiver, String path)
        
        {  
                //String rece=receiver;
                String filename= path;
                String rece= receiver;
                System.out.println(rece);
                //String filename = "/home/sleepyhead/asd.txt";
        	final String username = "teamasia.answeringmachine@gmail.com";
		final String password = "weloveperccom";
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("mohaimen.nahin@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
			InternetAddress.parse(rece));
			message.setSubject("New Voice Mail Message");
			message.setText("Dear Mail Crawler,"
				+ "\n\n No spam to my email, please!");
                        BodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart = new MimeBodyPart();
                        Multipart multipart = new MimeMultipart();
                        
                        DataSource source = new FileDataSource(filename);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(filename);
                        multipart.addBodyPart(messageBodyPart);
                        message.setContent(multipart);
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
        
        
        }
        
        //example usage:
	public static void main(String[] args) {
            String path="/home/sleepyhead/asd.txt";
                String receiver= "mohhos-4@student.ltu.se";
            SendMailTLS sm = new SendMailTLS();
            sm.sendmail(receiver,path);
            
	
	}
}