package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.PersonMessagesSlide;
import com.queatz.snappy.fragment.PersonUptoSlide;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonAdapter extends SlideScreen.SlideScreenAdapter {
    Person mPerson;
    String messagePrefill;

    public PersonAdapter(FragmentManager fragmentManager, Person person) {
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
