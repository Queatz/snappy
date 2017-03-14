package com.queatz.snappy.logic;

import com.arangodb.entity.BaseDocument;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthThing {
    private BaseDocument raw;

    public EarthThing(BaseDocument vPackSlice) {
        raw = vPackSlice;
    }

    public String getString(String field) {
        return (String) raw.getAttribute(field);
    }

    public boolean has(String field) {
        return raw.getProperties().containsKey(field);
    }

    public EarthRef key() {
        return new EarthRef(raw.getKey());
    }

    public EarthRef getKey(String field) {
        return new EarthRef((String) raw.getAttribute(field));
    }

    public boolean getBoolean(String field) {
        return (Boolean) raw.getAttribute(field);
    }

    public Date getDate(String field) {
        return (Date) raw.getAttribute(field);
    }

    public EarthGeo getGeo(String field) {
        return EarthGeo.of(
                (Double) ((List) raw.getAttribute(field)).get(0),
                (Double) ((List) raw.getAttribute(field)).get(1)
        );
    }

    public double getDouble(String field) {
        return (Double) raw.getAttribute(field);
    }

    public boolean isNull(String field) {
        return raw.getAttribute(field) == null;
    }

    public long getLong(String field) {
        return (Long) raw.getAttribute(field);
    }

    public static EarthThing from(@Nullable BaseDocument document) {
        if (document == null) {
            return null;
        }

        return new EarthThing(document);
    }

    public Builder edit() {
        return new Builder(raw);
    }

    public static class Builder {
        private BaseDocument raw;

        public Builder() {
            this.raw = new BaseDocument();
        }

        public Builder(BaseDocument raw) {
            this.raw = raw;
        }

        public Builder set(String field) {
            raw.updateAttribute(field, null);
            return this;
        }

        public Builder set(String field, EarthRef value) {
            raw.updateAttribute(field, value.name());
            return this;
        }

        public Builder set(String field, String value) {
            raw.updateAttribute(field, value);
            return this;
        }

        public Builder set(String field, boolean value) {
            raw.updateAttribute(field, value);
            return this;
        }

        public Builder set(String field, Date value) {
            raw.updateAttribute(field, value);
            return this;
        }

        public Builder set(String field, Number value) {
            raw.updateAttribute(field, value);
          return this;
        }

        public Builder set(String field, EarthGeo value) {
            raw.updateAttribute(field, ImmutableList.of(
                    value.getLatitude(),
                    value.getLongitude()
            ));
           return this;
        }

        public EarthThing build() {
            return new EarthThing(raw);
        }
    }
}
