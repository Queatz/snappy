package com.queatz.snappy.earth.concept;

import com.queatz.snappy.earth.access.As;

/**
 * Created by jacob on 3/26/16.
 */
public interface Joinable {
    boolean join(As access);
    boolean leave(As access);
}
