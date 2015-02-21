package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Party;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class RecentMessagesAdapter extends RealmBaseAdapter<Message> {
    public RecentMessagesAdapter(Context context, RealmResults<Message> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
