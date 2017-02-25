package com.queatz.snappy.team;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jacob on 2/24/17.
 */
public class ScrollActionsTouchListener implements View.OnTouchListener {
    private Boolean up;
    private OnScrollActions actions;

    public ScrollActionsTouchListener(OnScrollActions actions) {
        this.actions = actions;
    }

    @Override
    public boolean onTouch(android.view.View v, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_MOVE ||
                event.getHistorySize() < 1 ||
                event.getHistoricalY(0) == event.getY()) {
            return false;
        }

        boolean up = event.getY() > event.getHistoricalY(0);

        if (this.up == null || !this.up.equals(up)) {
            if (up) {
                actions.up();
            } else {
                actions.down();
            }

            this.up = up;
        }

        return false;
    }

    public void reset() {
        up = null;
    }
}
