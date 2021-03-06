package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.earth.EarthRef;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentEditor extends EarthControl {
    private final EarthStore earthStore;
    private final RecentMine recentMine;

    public RecentEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
        recentMine = use(RecentMine.class);
    }

    public EarthThing newRecent(EarthThing person, EarthThing with, EarthThing latest) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.RECENT_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, with.key())
                .set(EarthField.UPDATED_ON, new Date())
                .set(EarthField.SEEN, latest.getKey(EarthField.SOURCE).equals(person.key()))
                .set(EarthField.LATEST, latest.key()));
    }

    public void updateWithMessage(EarthThing message) {
        for(EarthRef[] fromTo : new EarthRef[][] {
                new EarthRef[] {
                        message.getKey(EarthField.SOURCE),
                        message.getKey(EarthField.TARGET)
                },
                new EarthRef[] {
                        message.getKey(EarthField.TARGET),
                        message.getKey(EarthField.SOURCE)
                },
        }) {
            EarthThing recent = recentMine.byPerson(fromTo[0], fromTo[1]);

            if(recent == null) {
                newRecent(earthStore.get(fromTo[0]), earthStore.get(fromTo[1]), message);
            } else {
                earthStore.save(earthStore.edit(recent)
                        .set(EarthField.UPDATED_ON, new Date())
                        .set(EarthField.LATEST, message.key())
                        .set(EarthField.SEEN, message.getKey(EarthField.SOURCE).equals(fromTo[0])));
            }
        }
    }

    public EarthThing markSeen(@NotNull EarthThing recent) {
        return earthStore.save(earthStore.edit(recent).set(EarthField.SEEN, true));
    }
}
