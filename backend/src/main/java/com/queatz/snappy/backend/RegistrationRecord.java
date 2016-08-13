package com.queatz.snappy.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * The Objectify object model for device registrations we are persisting (push notifications)
 */
@Entity
public class RegistrationRecord {
    @Id private Long id;
    @Index private String regId;
    @Index private String userId;
    @Index private String socialMode;
    @Index private Date created;
    @Index private Date updated;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}