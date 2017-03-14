package com.queatz.snappy.logic;

import com.arangodb.entity.BaseDocument;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthThing {
    private VPackSlice raw;

    public EarthThing(VPackSlice vPackSlice) {
        raw = vPackSlice;
    }

    public String getString(String field) {
        return raw.get(field).getAsString();
    }

    public boolean has(String field) {
        return true;
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

    public boolean isNull(String field) {
        return false;
    }

    public long getLong(String field) {
        return 0;
    }

    public static EarthThing from(@Nullable VPackSlice vPackSlice) {
        if (vPackSlice == null) {
            return null;
        }

        return new EarthThing(vPackSlice);
    }

    public Builder edit() {
        return new BaseDocument().setProperties();
    }

    public static class Builder {
        private VPackSlice raw;

        public Builder() {
            this.raw = new VPack.Builder().build();
        }

        public Builder(VPackSlice raw) {
            this.raw = raw;
        }

        public Builder set(String field) {
            raw.set();
            return this;
        }

        public Builder set(String field, EarthRef value) {
            raw.set();
            return this;
        }

        public Builder set(String field, String value) {
            raw.set();
            return this;
        }

        public Builder set(String field, boolean value) {
            raw.set();
            return this;
        }

        public Builder set(String field, Date value) {
            raw.set();
            return this;
        }

        public Builder set(String field, Number value) {
            raw.set();
            return this;
        }

        public Builder set(String field, EarthGeo geo) {
            raw.set();
            return this;
        }

        public EarthThing build() {
            return new EarthThing(raw);
        }
    }
}
