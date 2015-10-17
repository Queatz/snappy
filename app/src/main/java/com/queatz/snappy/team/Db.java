package com.queatz.snappy.team;

import io.realm.Realm;

/**
 * Created by jacob on 3/15/15.
 */
public class Db {
    public interface Call {
        void db(Realm realm);
        void post();
    }
}
