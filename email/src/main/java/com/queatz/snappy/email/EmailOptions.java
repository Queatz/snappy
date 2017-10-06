package com.queatz.snappy.email;

/**
 * Created by jacob on 5/8/17.
 */

public class EmailOptions {
    private String subject;
    private String header;
    private String body;
    private String footer;

    public String getSubject() {
        return subject;
    }

    public EmailOptions setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public EmailOptions setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getBody() {
        return body;
    }

    public EmailOptions setBody(String body) {
        this.body = body;
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public EmailOptions setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public String getCompleteEmail() {
        StringBuilder completeEmail = new StringBuilder();

        if (getHeader() != null) {
            completeEmail.append(getHeader());
        }

        if (getBody() != null) {
            completeEmail.append(getBody());
        }

        if (getFooter() != null) {
            completeEmail.append(getFooter());
        }

        return completeEmail.toString();
    }
}
