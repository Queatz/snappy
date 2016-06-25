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

    int emailDelay();
}
