package com.village.things;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.EarthUpdate;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactInterface extends CommonLinkInterface {

    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        as.requireUser();

        EarthThing contact;

        if (role != null) {
            contact = as.s(ContactEditor.class).newContact(target, source, role);
        } else {
            contact = as.s(ContactEditor.class).newContact(target, source);
        }

        new EarthUpdate(as).send(new NewContactEvent(as.getUser(), contact)).to(source);

        return contact;
    }

    @Override
    public EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return link; // TODO Edit role
    }
}
