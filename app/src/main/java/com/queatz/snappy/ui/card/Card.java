package com.queatz.snappy.ui.card;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.realm.RealmObject;

/**
 * Created by jacob on 11/12/15.
 */
public interface Card<T extends RealmObject> {
    View getCard(Context context, T realmObject, View convertView, ViewGroup parent);
}
