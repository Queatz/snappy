package com.queatz.snappy.util;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.ContextualInputBar;

/**
 * Created by jacob on 2/21/17.
 */

public class WantContextualBehavior implements ContextualBehavior {
    @Override
    public void use(final ContextualInputBar contextualInputBar) {
        contextualInputBar.setHint(R.string.what_do_you_want);

        contextualInputBar.setSendAction(new Runnable() {
            @Override
            public void run() {
                contextualInputBar.postAsWant();
            }
        });
    }

    @Override
    public void dispose(ContextualInputBar contextualInputBar) {
        contextualInputBar.resetAll();
    }
}
