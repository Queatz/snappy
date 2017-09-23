package com.queatz.snappy.chat;

import android.location.Location;

/**
 * Created by jacob on 9/23/17.
 */

public interface OnChatChangedCallback {
    void onContentChanged();
    void onLocationChanged(Location location);
}
