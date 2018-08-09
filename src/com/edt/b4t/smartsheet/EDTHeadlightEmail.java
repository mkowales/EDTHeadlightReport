package com.edt.b4t.smartsheet;

import java.io.IOException;
import java.util.ArrayList;

import com.edt.email.Outlook365Email;

public class EDTHeadlightEmail extends Outlook365Email
{
	public EDTHeadlightEmail()
	        throws IOException
    {
		super("props/EDTHeadlight");
		
/*
		this.header = System.getProperty("image.header");
		
	    try
	    {
			String user = System.getProperty("email.smtp.user"),
					pwd = System.getProperty("email.smtp.mrk"),
					
					to = System.getProperty("ss.email.to"),
					cc = System.getProperty("ss.email.cc"),
					bcc = System.getProperty("ss.email.bcc"),
					from = System.getProperty("ss.email.from"),
					subject = System.getProperty("ss.email.subject");
	
	        Session session = Session.getInstance(System.getProperties(), new javax.mail.Authenticator()
	        {
	            protected PasswordAuthentication getPasswordAuthentication()
	            {
	                return new PasswordAuthentication(user, pwd);
	            }
	        });

            this.message = new MimeMessage(session);
            this.message.setFrom(new InternetAddress(from));
            this.message.setRecipients(Message.RecipientType.TO, InternetAddress.parse((null != to) ? to : user));
            this.message.setRecipients(Message.RecipientType.CC, InternetAddress.parse((null != cc) ? cc : user));
            this.message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse((null != bcc) ? bcc : user));
            this.message.setSubject(subject);
            
            System.out.println("Preparing to send " + subject + " from " + from + " to " + ((null != to) ? to : user) + "...");
        }
        catch (MessagingException e)
        {
        	e.printStackTrace();
        }
*/
	}
	
//	public void send(String body)
//    {
//	    try
//	    {
//            if (null != body)
//            {
//	            this.message.setContent(body, "text/html");
//	    		Transport.send(this.message);
//	    		System.out.println("Successful!!");
//            }
//            else
//            	System.out.println("NO email to send.");
//        }
//        catch (MessagingException e)
//        {
//        	e.printStackTrace();
//        }
//	}

	public String getBody(ArrayList<EDTHeadlightProps> headlights, String header, boolean image)
	{
		String content = "<center>";
		
		if (image)
			content += "<img src=" + System.getProperty("image.header") + ">";
				
		content += "<h2>" + header + "</h2>" +
				"<table border=\"1\" cellpadding=\"5\">" +
				"<thead>" +
				"<tr align=\"center\">" +
				"<td bgcolor=\"midnight\"><font color=\"white\">Project</font></td>" +
				"<td bgcolor=\"midnight\"><font color=\"white\">Project ID (B4T)</font></td>" +
				"<td bgcolor=\"midnight\"><font color=\"white\">Client Name</font></td>" +
				"<td bgcolor=\"midnight\"><font color=\"white\">Service Area</font></td>" +
				"<td bgcolor=\"midnight\"><font color=\"white\">Project Owner</font></td>" +
				"</tr>" +
				"</thead>";

		String body = "";

		for (int element = 0; element < headlights.size(); element++)
		{
			body += "<tr align=\"center\">" + 
					"<td>" + headlights.get(element).getProjectName() + "</a></td>" +
					"<td>" + headlights.get(element).getProjectId() + "</td>" +
					"<td>" + headlights.get(element).getClientName() + "</td>" +
					"<td>" + headlights.get(element).getServiceArea() + "</td>" +
					"<td>" + headlights.get(element).getProjectOwner() + "</a></td>" +
					"</tr>";
		}
			
		body += "</table></center><br>";

		return (content + body);
	}
}
