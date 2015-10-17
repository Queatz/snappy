package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class QuestLinkSpec extends ThingSpec {
    public @Hide Key<PersonSpec> personId;
    public @Hide Key<QuestSpec> questId;

    public @Ignore PersonSpec person;
    public @Ignore QuestSpec quest;
}
