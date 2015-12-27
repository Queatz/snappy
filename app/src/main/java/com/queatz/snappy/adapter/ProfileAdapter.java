package com.queatz.snappy.adapter;

import android.app.Fragment;
import android.app.FragmentManager;

import com.queatz.snappy.fragment.PersonMessagesSlide;
import com.queatz.snappy.fragment.PersonUptoSlide;
import com.queatz.snappy.fragment.SettingsSlide;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.ui.SlideScreen;

/**
 * Created by jacob on 12/6/15.
 */
public class ProfileAdapter extends SlideScreen.SlideScreenAdapter {
    Person mPerson;

    public ProfileAdapter(FragmentManager fragmentManager, Person person) {
        super(fragmentManager);

        mPerson = person;
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
                SettingsSlide settingsSlide = new SettingsSlide();
                settingsSlide.setPerson(mPerson);

                return settingsSlide;
            default:
                return null;
        }
    }
}
