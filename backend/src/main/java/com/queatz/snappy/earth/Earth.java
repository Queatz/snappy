package com.queatz.snappy.earth;

import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.shared.things.PersonSpec;

/**
 * Created by jacob on 3/26/16.
 */

public class Earth {
    public static As as(final PersonSpec person) {
        return new As(person);
    }
}
