package com.queatz.snappy.shared.things;

import com.googlecode.objectify.annotation.Entity;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class GroupSpec extends ThingSpec {
    public String name;
}
