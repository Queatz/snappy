package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.eventables.NewContactEvent;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactInterface extends CommonLinkInterface {

    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String role) {
        as.requireUser();

        EarthThing contact;

        if (role != null) {
            contact = new ContactEditor(as).newContact(target, source, role);
        } else {
            contact = new ContactEditor(as).newContact(target, source);
        }

        new EarthUpdate(as).send(new NewContactEvent(as.getUser(), contact)).to(source);

        return contact;
    }

    @Override
    public EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return link; // TODO Edit role
    }
}
