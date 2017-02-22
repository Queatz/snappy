package com.queatz.snappy.util;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.ContextualInputBar;

/**
 * Created by jacob on 2/21/17.
 */

public class DoingContextualBehavior implements ContextualBehavior {

    @Override
    public void use(final ContextualInputBar contextualInputBar) {
        contextualInputBar.setHint(R.string.what_are_you_doing);

        contextualInputBar.setSendAction(new Runnable() {
            @Override
            public void run() {
                contextualInputBar.postAsUpdate();
            }
        });

        contextualInputBar.enabmeAutocomplete(true);
        contextualInputBar.showCamera(true);
        contextualInputBar.showInfo(true);
    }

    public void dispose(ContextualInputBar contextualInputBar) {
        contextualInputBar.showInfo(false);
        contextualInputBar.showCamera(false);
        contextualInputBar.enabmeAutocomplete(false);
        contextualInputBar.resetAll();
    }
}
