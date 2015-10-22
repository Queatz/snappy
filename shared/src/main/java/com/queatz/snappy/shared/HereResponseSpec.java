package com.queatz.snappy.shared;

import com.queatz.snappy.shared.things.LocationSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestSpec;

import java.util.List;

/**
 * Created by jacob on 10/15/15.
 */
public class HereResponseSpec {
    public @Deep List<PartySpec> parties;
    public @Deep List<PersonSpec> people;
    public @Deep List<LocationSpec> locations;
    public @Deep List<QuestSpec> quests;
}
