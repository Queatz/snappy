package com.queatz.snappy.things;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by jacob on 2/14/15.
 */
public class Person extends RealmObject implements Thing {
    @Index
    private String id;
    private String firstName;
    private String lastName;
    private String about;
    private String imageUrl;
    private long infoFollowers;
    private long infoFollowing;
    private long infoHosted;
    private RealmList<Update> updates;
    private RealmList<Message> messages;

    public String getName() {
        return getFirstName() + " " + getLastName();
    }

    public String getImageUrlForSize(int size) {
        if(getImageUrl() == null || getImageUrl().isEmpty() || !getImageUrl().contains("="))
            return null;

        return getImageUrl().split("=")[0] + "=" + size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getInfoFollowers() {
        return infoFollowers;
    }

    public void setInfoFollowers(long infoFollowers) {
        this.infoFollowers = infoFollowers;
    }

    public long getInfoFollowing() {
        return infoFollowing;
    }

    public void setInfoFollowing(long infoFollowing) {
        this.infoFollowing = infoFollowing;
    }

    public long getInfoHosted() {
        return infoHosted;
    }

    public void setInfoHosted(long infoHosted) {
        this.infoHosted = infoHosted;
    }

    public RealmList<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(RealmList<Update> updates) {
        this.updates = updates;
    }

    public RealmList<Message> getMessages() {
        return messages;
    }

    public void setMessages(RealmList<Message> messages) {
        this.messages = messages;
    }
}
