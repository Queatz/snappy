package com.queatz.snappy.logic;

import com.arangodb.entity.BaseDocument;

import java.util.Date;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthThing {
    private BaseDocument raw;

    public String getString(String field) {
        return null;
    }

    public boolean has(String field) {
        return true;
    }

    @Deprecated
    public boolean contains(String field) {
        return has(field);
    }

    public EarthRef key() {
        return null;
    }

    public EarthRef getKey(String field) {
        return null;
    }

    public boolean getBoolean(String field) {
        return false;
    }

    public Date getDate(String field) {
        return null;
    }

    public EarthGeo getGeo(String field) {
        return null;
    }

    public double getDouble(String field) {
        return 0d;
    }

    @Deprecated
    public Date getDateTime(String date) {
        return getDate(date);
    }

    @Deprecated
    public EarthGeo getLatLng(String geo) {
        return getGeo(geo);
    }

    public boolean isNull(String field) {
        return false;
    }

    public long getLong(String field) {
        return 0;
    }

    public class Builder {

        public Builder set(String field) {
            return null;
        }

        public Builder set(String field, EarthRef value) {
            return null;
        }

        public Builder set(String field, String value) {
            return null;
        }

        public Builder set(String field, boolean value) {
            return null;
        }

        public Builder set(String field, Date value) {
            return null;
        }

        public Builder set(String field, Number value) {
            return null;
        }

        public Builder set(String field, EarthGeo geo) {
            return null;
        }
    }
}
