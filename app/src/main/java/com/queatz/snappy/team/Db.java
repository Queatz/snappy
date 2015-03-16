package com.queatz.snappy.team;

import io.realm.Realm;

/**
 * Created by jacob on 3/15/15.
 */
public class Db {
    public static interface Call {
        public void db(Realm realm);
        public void post();
    }
}
