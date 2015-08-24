package com.queatz.snappy.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The Objectify object model for device registrations we are persisting
 */
@Entity
public class RegistrationRecord {
    @Id
    Long id;

    @Index
    private String regId;

    @Index
    private String userId;

    @Index
    private String socialMode;

    public RegistrationRecord() {
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSocialMode() {
        return socialMode;
    }

    public void setSocialMode(String socialMode) {
        this.socialMode = socialMode;
    }
}