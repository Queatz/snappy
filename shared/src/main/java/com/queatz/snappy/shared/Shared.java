package com.queatz.snappy.shared;

import java.util.Random;

/**
 * Created by jacob on 9/17/17.
 */

public class Shared {
    public static String randomToken() {
        Random random = new Random();
        return Long.toString(random.nextLong()) +
                Long.toString(random.nextLong()) +
                Long.toString(random.nextLong());
    }
}
