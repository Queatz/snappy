package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.exceptions.LogicException;

import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
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
    public void sendRawEmail(Entity fromPerson,
                             Entity toPerson,
                             String subject,
                             String message) {
        sendRawEmail(
                fromPerson.getString(EarthField.EMAIL),
                toPerson.getString(EarthField.EMAIL),
                subject,
                message);
    }

    public void sendRawEmail(String fromPerson,
                             String toPerson,
                             String subject,
                             String message) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromPerson));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toPerson));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
        } catch (MessagingException e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
