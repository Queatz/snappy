package com.queatz.snappy.logic.concepts;

/**
 * Generates notifications for events that happen.
 *
 * Created by jacob on 6/19/16.
 */
public interface Eventable {

    /**
     * Creates an object that will be serialized as JSON and sent to client devices to make
     * push notifications.
     */
    Object makePush();

    /**
     * Creates an email subject from the event
     */
    String makeSubject();

    /**
     * Creates an email message body from the event
     */
    String makeEmail();

    /**
     * Recreates the event from a string
     */
    Eventable fromData(String data);

    /**
     * Represents the event as a string. The event must be able to be fully recreated from this.
     */
    String toData();

    /**
     * The delay before sending the email. This allows the email to be cancelled if they undo
     * whatever caused the email
     *
     * Currently unused!
     */
    int emailDelay();
}
