package com.queatz.snappy.logic.mines;

import com.google.common.collect.Lists;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.shared.Config;

import java.util.List;

/**
 * Created by jacob on 5/15/16.
 */
public class MessageMine extends EarthControl {
    public MessageMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> messagesFromTo(EarthRef source, EarthRef target) {
        return Lists.newArrayList(
                use(EarthStore.class).queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, source),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, target)
                )
        );
    }

    public List<EarthThing> messagesFrom(EarthRef source) {
        return Lists.newArrayList(
                use(EarthStore.class).queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, source)
                )
        );
    }

    public List<EarthThing> messagesTo(EarthRef target) {
        return Lists.newArrayList(
                use(EarthStore.class).queryLimited(Config.SEARCH_MAXIMUM,
                        StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.MESSAGE_KIND),
                        StructuredQuery.PropertyFilter.eq(EarthField.TARGET, target)
                )
        );
    }
}
