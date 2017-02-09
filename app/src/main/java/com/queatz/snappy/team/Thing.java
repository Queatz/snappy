package com.queatz.snappy.team;

/**
 * Created by jacob on 8/14/16.
 */

public class Thing {
    public static final String ID = "id";
    public static final String LOCATION = "location";

    /**
     * The kind of the thing.
     */
    public static final String KIND = "kind";

    /**
     * The time it was when the thing was created.
     */
    public static final String CREATED_ON = "createdOn";

    /**
     * A name for the thing.
     */
    public static final String NAME = "name";

    /**
     * A short description about this thing.
     */
    public static final String ABOUT = "about";

    /**
     * A boolean of whether or not a photo is associated with this thing.
     */
    public static final String PHOTO = "photo";

    /**
     * An address for a thing.
     */
    public static final String ADDRESS = "address";

    /**
     * A latitude.
     * @see GEO
     */
    public static final String LATITUDE = "latitude";

    /**
     * A longitude.
     * @see GEO
     */
    public static final String LONGITUDE = "longitude";

    /**
     * The geospatial location of a thing.
     * @see LATITUDE
     * @see LONGITUDE
     */
    public static final String GEO = "geo";

    /**
     * The last time a thing was around.
     */
    public static final String AROUND = "around";

    /**
     * The source of a link.
     */
    public static final String SOURCE = "source";

    /**
     * The target of a link.
     */
    public static final String TARGET = "target";

    /**
     * The value of an offer.
     */
    public static final String PRICE = "price";

    /**
     * The unit that value is applicable to.
     */
    public static final String UNIT = "unit";

    /**
     * A type of subscription.
     */
    public static final String SUBSCRIPTION = "subscription";

    /**
     * The last time something was updated.
     */
    public static final String UPDATED = "updated";

    /**
     * Whether or not this object was seen by the recipient.
     */
    public static final String SEEN = "seen";

    /**
     * The latest of a thing.
     */
    public static final String LATEST = "latest";

    /**
     * A message associated with a thing.
     */
    public static final String MESSAGE = "message";

    /**
     * The status of a link.
     */
    public static final String STATUS = "status";

    /**
     * The original this was copied from.
     */
    public static final String ORIGINAL = "original";

    /**
     * The date associated with the thing.
     */
    public static final String DATE = "date";

    /**
     * Whether or not a thing is open for new members or not.
     */
    public static final String FULL = "full";

    /**
     * The host of something.
     */
    public static final String HOST = "host";

    /**
     * The action that was taken.
     */
    public static final String ACTION = "action";

    /**
     * The role something plays in a relationship to something else.
     */
    public static final String ROLE = "role";

    /**
     * Other, less generic fields.
     */
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String IMAGE_URL = "imageUrl";
    public static final String GOOGLE_URL = "googleUrl";
    public static final String TOKEN = "token";
    public static final String EMAIL = "email";
    public static final String GENDER = "gender";
    public static final String LANGUAGE = "language";
    public static final String GOOGLE_ID = "googleId";


    // From views
    public static final String LIKERS = "likers";
    public static final String JOINS = "joins";
    public static final String OFFERS = "offers";
    public static final String UPDATES = "updates";
    public static final String INFO_FOLLOWERS = "infoFollowers";
    public static final String INFO_FOLLOWING = "infoFollowing";
    public static final String INFO_DISTANCE = "infoDistance";
    public static final String INFO_UPDATED = "infoUpdated";
    public static final String SOCIAL_MODE = "socialMode";
    public static final String PERSON = "person";
    public static final String TO = "to";
    public static final String FROM = "from";
    public static final String CONTACTS = "contacts";

    public static final String AUTH = "auth";
    public static final String PLACEHOLDER = "placeholder";
    public static final String ASPECT = "aspect";
    public static final String WITH = "with";

    // Local only
    public static final String LOCAL_STATE = "localState";
}
