package com.queatz.snappy.util;

import com.queatz.snappy.ui.ContextualInputBar;

/**
 * Created by jacob on 2/21/17.
 */

public interface ContextualBehavior {
    void use(ContextualInputBar contextualInputBar);
    void dispose(ContextualInputBar contextualInputBar);
}
