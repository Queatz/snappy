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
    public static final String subscriptionProductId = "com.queatz.snappy.monthly";

    public static final String PREFERENCE_USER = "auth.user";
    public static final String PREFERENCE_AUTH_TOKEN = "auth.auth_token";
    public static final String PREFERENCE_GCM_REGISTRATION_ID = "auth.gcm_registration_id";
    public static final String PREFERENCE_HOST_PARTY_SCREEN_SHOWN = "other.party_screen_shown";
    public static final String PREFERENCE_HOSTING_ENABLED = "other.hosting_enabled";

    public static final String HOSTING_ENABLED_FALSE = Boolean.toString(false);
    public static final String HOSTING_ENABLED_AVAILABLE = "available";
    public static final String HOSTING_ENABLED_TRUE = Boolean.toString(true);

    public static final int REQUEST_CODE_AUTH_RESOLUTION = 1;
    public static final int REQUEST_CODE_ACCOUNT_PICKER = 2;
    public static final int REQUEST_CODE_BUY_INTENT = 3;
    public static final int REQUEST_CODE_PLAY_SERVICES = 4;
    public static final int REQUEST_CODE_CHECK_SETTINGS = 5;

    public static final String PUSH_ACTION_JOIN_REQUEST = "join_requested";
    public static final String PUSH_ACTION_JOIN_ACCEPTED = "join_accepted";
    public static final String PUSH_ACTION_HOSTING_REMINDER = "hosting_reminder";
    public static final String PUSH_ACTION_FOLLOW = "follow";
    public static final String PUSH_ACTION_MESSAGE = "message";
    public static final String PUSH_ACTION_NEW_PARTY = "new_party";
    public static final String PUSH_ACTION_JOIN_PARTY = "join_party";
    public static final String PUSH_ACTION_REFRESH_ME = "refresh_me";
    public static final String PUSH_ACTION_CLEAR_NOTIFICATION = "clear_notification";

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_PARTY = "party";
    public static final String EXTRA_JOIN = "join";
    public static final String EXTRA_ACTION_JOIN_ACCEPT = "join.accept";
    public static final String EXTRA_ACTION_JOIN_REQUEST = "join.request";

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
    public static final String PARAM_PURCHASE_DATA = "purchase_data";

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
    public static final String PATH_ME_BUY= "me/buy";
    public static final String PATH_ME_REGISTER_DEVICE = "me/register_device";
    public static final String PATH_ME_UNREGISTER_DEVICE = "me/unregister_device";
    public static final String PATH_ME_CLEAR_NOTIFICATION = "me/clear_notification";

    public static final String JOIN_STATUS_REQUESTED = "requested";
    public static final String JOIN_STATUS_IN = "in";
    public static final String JOIN_STATUS_OUT = "out";
    public static final String JOIN_STATUS_WITHDRAWN = "withdrawn";

    public static final int BILLING_RESPONSE_RESULT_OK                  = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED       = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE    = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR     = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR               = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED  = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED      = 8;
    public static final String BILLING_RESPONSE_CODE                    = "RESPONSE_CODE";
    public static final String BILLING_DETAILS_LIST                     = "DETAILS_LIST";
    public static final String BILLING_BUY_INTENT                       = "BUY_INTENT";
    public static final String BILLING_INAPP_PURCHASE_DATA              = "INAPP_PURCHASE_DATA";
    public static final String BILLING_INAPP_DATA_SIGNATURE             = "INAPP_DATA_SIGNATURE";
    public static final String BILLING_INAPP_PURCHASE_ITEM_LIST         = "INAPP_PURCHASE_ITEM_LIST";
    public static final String BILLING_INAPP_PURCHASE_DATA_LIST         = "INAPP_PURCHASE_DATA_LIST";
    public static final String BILLING_INAPP_DATA_SIGNATURE_LIST        = "INAPP_DATA_SIGNATURE_LIST";
    public static final String BILLING_INAPP_CONTINUATION_TOKEN         = "INAPP_CONTINUATION_TOKEN";
    public static final String BILLING_ITEM_ID_LIST                     = "ITEM_ID_LIST";
}