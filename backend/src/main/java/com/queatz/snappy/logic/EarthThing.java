package com.queatz.snappy.logic;

import com.arangodb.entity.BaseDocument;
import com.arangodb.velocypack.internal.util.DateUtil;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthThing {
    private BaseDocument raw;
    private String localId;

    public EarthThing(BaseDocument thing) {
        raw = thing;
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
        try {
            return DateUtil.parse((String) raw.getAttribute(field));
        } catch (ParseException e) {
            return null;
        }
    }

    public EarthGeo getGeo(String field) {
        return EarthGeo.of(
                (Double) ((List) raw.getAttribute(field)).get(0),
                (Double) ((List) raw.getAttribute(field)).get(1)
        );
    }

    public double getDouble(String field) {
        return getNumber(field).doubleValue();
    }

    public boolean isNull(String field) {
        return raw.getAttribute(field) == null;
    }

    public Number getNumber(String field) {
        return (Number) raw.getAttribute(field);
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

    protected BaseDocument getRaw() {
        return raw;
    }

    public String getLocalId() {
        return localId;
    }

    public EarthThing setLocalId(String localId) {
        this.localId = localId;
        return this;
    }

    public static class Builder {
        private BaseDocument raw;
        private boolean create;

        public Builder() {
            this.raw = new BaseDocument();
            create = true;
        }

        public Builder(BaseDocument raw) {
            this.raw = raw;
            create = false;
        }

        public Builder set(String field) {
            raw.addAttribute(field, null);
            return this;
        }

        public Builder set(String field, EarthRef value) {
            raw.addAttribute(field, value.name());
            return this;
        }

        public Builder set(String field, String value) {
            raw.addAttribute(field, value);
            return this;
        }

        public Builder set(String field, boolean value) {
            raw.addAttribute(field, value);
            return this;
        }

        public Builder set(String field, Date value) {
            raw.addAttribute(field, DateUtil.format(value));
            return this;
        }

        public Builder set(String field, Number value) {
            raw.addAttribute(field, value);
          return this;
        }

        public Builder set(String field, EarthGeo value) {
            raw.addAttribute(field, ImmutableList.of(
                    value.getLatitude(),
                    value.getLongitude()
            ));
           return this;
        }

        public BaseDocument build() {
            return raw;
        }

        protected boolean isCreate() {
            return create;
        }
    }
}
