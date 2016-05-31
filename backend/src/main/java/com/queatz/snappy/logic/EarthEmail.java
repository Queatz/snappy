package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 5/29/16.
 */
public class EarthEmail {
    private void sendEmail(Entity toPerson, HttpServletRequest req) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        String msgBody = "I want one too!\n\nReply at http://localhost:3000/resources/-3148995159308430153\n\nDo not reply to this email.";

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("my@village.city"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toPerson.getString(EarthField.EMAIL)));
        msg.setSubject("Kaicy posted on Betty Boop's Left Boob");
        msg.setText(msgBody);
        Transport.send(msg);
    }
}
