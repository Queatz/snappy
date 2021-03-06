package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.PersonMessagesSlide;
import com.queatz.snappy.fragment.PersonUptoSlide;
import com.queatz.snappy.ui.slidescreen.SlideScreen;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonAdapter extends SlideScreen.SlideScreenAdapter {
    DynamicRealmObject mPerson;
    String messagePrefill;

    public PersonAdapter(FragmentManager fragmentManager, DynamicRealmObject person) {
        super(fragmentManager);

        mPerson = person;
    }

    public void setMessagePrefill(String message) {
        messagePrefill = message;
    }

    public int getCount() {
        return 2;
    }

    public Fragment getSlide(int page) {
        switch (page) {
            case 0:
                PersonUptoSlide personUptoSlide = new PersonUptoSlide();
                personUptoSlide.setPerson(mPerson);

                return personUptoSlide;
            case 1:
                PersonMessagesSlide personMessagesSlide = new PersonMessagesSlide();
                personMessagesSlide.setPerson(mPerson);
                personMessagesSlide.setMessagePrefill(messagePrefill);

                return personMessagesSlide;
            default:
                return null;
        }
    }
}
