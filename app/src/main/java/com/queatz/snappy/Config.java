package com.queatz.snappy;

/**
 * Created by jacob on 10/25/14.
 */
public class Config {
    public static final String LOG_TAG = "SNAPPY_LOG";
    public static final String API_URL = "http://queatz-snappy.appspot.com/api/";
    public static final String BACKEND_URL = "https://queatz-snappy.appspot.com/_ah/api/";
    public static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?location=%f,%f&radius=1609&input=%s&key=%s";
    public static final String GOOGLE_PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&key=%s";
    public static final int maxHoursInFuture = 18;
    public static final float locationAccuracy = 100;
    public static final float defaultMapZoom = 18;

    public static final String PREFERENCE_USER = "auth.user";
    public static final String PREFERENCE_AUTH_TOKEN = "auth.auth_token";
    public static final String PREFERENCE_GCM_REGISTRATION_ID = "auth.gcm_registration_id";

    public static final int REQUEST_CODE_AUTH_RESOLUTION = 1;
    public static final int REQUEST_CODE_ACCOUNT_PICKER = 2;
    public static final int REQUEST_CODE_BUY_INTENT = 3;

    public static final String PUSH_ACTION_JOIN_REQUEST = "join_requested";
    public static final String PUSH_ACTION_JOIN_ACCEPTED = "join_accepted";
    public static final String PUSH_ACTION_HOSTING_REMINDER = "hosting_reminder";
    public static final String PUSH_ACTION_FOLLOW = "follow";
    public static final String PUSH_ACTION_MESSAGE = "message";

    public static final String PARAM_AUTH = "auth";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_JOIN = "join";
    public static final String PARAM_CANCEL_JOIN = "cancel_join";
    public static final String PARAM_FULL = "full";
    public static final String PARAM_ACCEPT = "accept";
    public static final String PARAM_HIDE = "hide";
    public static final String PARAM_FOLLOW = "follow";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_SEEN = "seen";
    public static final String PARAM_LOCAL_ID = "local_id";
    public static final String PARAM_DEVICE_ID = "device_id";

    public static final String PATH_API = "api";
    public static final String PATH_PARTIES = "parties";
    public static final String PATH_PARTY = "party";
    public static final String PATH_PARTY_ID = "party/%s";
    public static final String PATH_JOIN_ID = "join/%s";
    public static final String PATH_PEOPLE_ID = "people/%s";
    public static final String PATH_PEOPLE = "people";
    public static final String PATH_SEARCH = "search";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_MESSAGES_ID = "messages/%s";
    public static final String PATH_UPTO = "upto";
    public static final String PATH_ME= "me";
    public static final String PATH_ME_UPTO= "me/upto";
    public static final String PATH_ME_REGISTER_DEVICE = "me/register_device";
    public static final String PATH_ME_UNREGISTER_DEVICE = "me/unregister_device";

    public static final String JOIN_STATUS_REQUESTED = "requested";
    public static final String JOIN_STATUS_IN = "in";
    public static final String JOIN_STATUS_OUT = "out";
    public static final String JOIN_STATUS_WITHDRAWN = "withdrawn";
}
