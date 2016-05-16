package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.collect.Lists;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/15/16.
 */
public class MessageMine {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public List<Entity> messagesFromTo(Key source, Key target) {
        return Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, source),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, target)
                )
        );
    };
    public List<Entity> messagesFrom(Key source) {
        return Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, source)
                )
        );
    };
    public List<Entity> messagesTo(Key target) {
        return Lists.newArrayList(
                earthStore.queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, target)
                )
        );
    };
}
