package com.queatz.snappy.chat;

/**
 * Created by jacob on 9/23/17.
 */

public class SendPhotoToTopic {

    private String topic;
    private byte[] file;

    public SendPhotoToTopic(String topic, byte[] file) {
        this.topic = topic;
        this.file = file;
    }

    public String getTopic() {
        return topic;
    }

    public SendPhotoToTopic setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public byte[] getFile() {
        return file;
    }

    public SendPhotoToTopic setFile(byte[] file) {
        this.file = file;
        return this;
    }
}
