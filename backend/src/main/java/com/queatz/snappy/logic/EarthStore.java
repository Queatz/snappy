package com.queatz.snappy.logic;

import com.google.gcloud.datastore.Datastore;
import com.google.gcloud.datastore.DatastoreException;
import com.google.gcloud.datastore.DatastoreOptions;
import com.google.gcloud.datastore.DateTime;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.Key;
import com.google.gcloud.datastore.KeyFactory;
import com.google.gcloud.datastore.NullValue;
import com.google.gcloud.datastore.StringValue;
import com.google.gcloud.datastore.Transaction;

import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthStore {

    private static final String DEFAULT_KIND = "Thing";
    public static final String DEFAULT_FIELD_KIND = EarthField.KIND;
    public static final String DEFAULT_FIELD_CREATED = "created_on";
    public static final String DEFAULT_FIELD_CONCLUDED = "concluded_on";

    private final Datastore datastore = DatastoreOptions.defaultInstance().service();
    private final KeyFactory keyFactory = datastore.newKeyFactory().kind(DEFAULT_KIND);

    /**
     * Get a thing from the store.
     *
     * - Fails if the thing has already concluded.
     *
     * @return The thing, or null if it could not be found
     */
    public Entity get(@Nonnull String id) {
        Entity entity = datastore.get(keyFactory.newKey(id));

        if (entity == null) {
            return null;
        }

        // Entity has already concluded, don't allow access anymore
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return null;
        }

        return entity;
    }

    /**
     * Put a thing into the store.
     *
     * - Does not modify the entity.
     * - Fails if the thing doesn't have a creation date.
     * - Fails if the thing has already concluded
     */
    public void put(@Nonnull Entity entity) {
        if (entity.key().name().isEmpty()) {
            return;
        }

        // Don't allow saving entities without a created date
        try {
            entity.getDateTime(DEFAULT_FIELD_CREATED);
        } catch (DatastoreException e) {
            e.printStackTrace();
            return;
        }

        // Don't allow saving concluded entities, use conclude() instead
        if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
            return;
        }

        datastore.put(entity);
    }

    /**
     * Create a thing of the specified kind.
     *
     * - Creates a new id for the thing.
     * - Sets the creation date.
     *
     * @return The new thing
     */
    public Entity create(@Nonnull String kind) {
        Key key = keyFactory.newKey(newRandomId());
        Entity entity = Entity.builder(key)
                .set(DEFAULT_FIELD_CREATED, DateTime.now())
                .set(DEFAULT_FIELD_CONCLUDED, NullValue.of())
                .set(DEFAULT_FIELD_KIND, StringValue.of(kind))
                .build();
        datastore.put(entity);
        return entity;
    }

    /**
     * Conclude a thing by it's id.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public void conclude(@Nonnull String id) {
        Transaction transaction = datastore.newTransaction();

        try {
            Entity entity = transaction.get(keyFactory.newKey(id));

            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return;
            }

            transaction.put(Entity.builder(entity).set(DEFAULT_FIELD_CONCLUDED, DateTime.now()).build());
            transaction.commit();
        } finally {
            if (transaction.active()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public Entity.Builder edit(@Nonnull Entity entity) {
        return Entity.builder(entity);
    }

    /**
     * Save a thing.
     *
     * - Assumes external validation happens.
     * - Fails if the thing already concluded.
     */
    public Entity save(@Nonnull Entity.Builder entityBulder) {
        Entity entity = entityBulder.build();

        Transaction transaction = datastore.newTransaction();

        try {
            // Don't allow concluding entities that have already concluded
            if (!entity.isNull(DEFAULT_FIELD_CONCLUDED)) {
                return entity;
            }

            transaction.put(entity);
            transaction.commit();
        } finally {
            if (transaction.active()) {
                transaction.rollback();
            }
        }

        return entity;
    }

    public final String newRandomId() {
        return Long.toString(new Random().nextLong());
    }
}
