package com.queatz.snappy.logic.concepts;

/**
 * Created by jacob on 6/19/16.
 */
public interface Eventable {

    Object makePush();
    String makeSubject();
    String makeEmail();

    // Serialization
    Eventable fromData(String data);
    String toData();

    // The delay before sending the email. This allows the email to be cancelled if they undo
    // whatever caused the email.
    int emailDelay();
}
