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
    public List<PartySpec> parties;
    public List<PersonSpec> people;
    public List<LocationSpec> locations;
    public List<QuestSpec> quests;
}
