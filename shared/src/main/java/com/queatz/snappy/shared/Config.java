package com.queatz.snappy.shared;

/**
 * Created by jacob on 10/25/14.
 */
public class Config {

    // Android

    public static final boolean BETA_VERSION = true;

    public static final String LOG_TAG = "SNAPPY_LOG";
    public static final String API_URL = (BETA_VERSION ? "https://beta-dot-queatz-snappy.appspot.com/api/" : "http://queatz-snappy.appspot.com/api/");
    public static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?location=%f,%f&radius=1609&input=%s&key=%s";
    public static final String GOOGLE_PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&key=%s";
    public static final int maxRequestRetries = 4;
    public static final int maxHoursInFuture = 18;
    public static final float locationAccuracy = 100;
    public static final float defaultMapZoom = 18;

    public static final String PREFERENCE_USER = "auth.user";
    public static final String PREFERENCE_AUTH_TOKEN = "auth.auth_token";
    public static final String PREFERENCE_GCM_REGISTRATION_ID = "auth.gcm_registration_id";
    public static final String PREFERENCE_HOST_PARTY_SCREEN_SHOWN = "other.party_screen_shown";
    public static final String PREFERENCE_HOSTING_ENABLED = "other.hosting_enabled";
    public static final String PREFERENCE_SOCIAL_MODE = "other.social_mode";
    public static final String PREFERENCE_APP_VERSION = "app.version";

    public static final int REQUEST_CODE_AUTH_RESOLUTION = 1;
    public static final int REQUEST_CODE_ACCOUNT_PICKER = 2;
    public static final int REQUEST_CODE_BUY_INTENT = 3;
    public static final int REQUEST_CODE_PLAY_SERVICES = 4;
    public static final int REQUEST_CODE_CHECK_SETTINGS = 5;
    public static final int REQUEST_CODE_CHOOSER = 6;

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_PARTY_ID = "party";
    public static final String EXTRA_JOIN_ID = "join";
    public static final String EXTRA_ACTION_JOIN_ACCEPT = "join.accept";
    public static final String EXTRA_ACTION_JOIN_REQUEST = "join.request";

    public static final String PATH_BOUNTY_ID = "bounty/%s";
    public static final String PATH_QUEST_ID = "quest/%s";
    public static final String PATH_PARTY_ID = "party/%s";
    public static final String PATH_JOIN_ID = "join/%s";
    public static final String PATH_PEOPLE_ID = "people/%s";
    public static final String PATH_PEOPLE_FOLLOWING = "people/%s/following";
    public static final String PATH_PEOPLE_FOLLOWERS = "people/%s/followers";
    public static final String PATH_PEOPLE_PARTIES = "people/%s/parties";
    public static final String PATH_SEARCH = "search";
    public static final String PATH_UPDATE_PHOTO = "update/%s/photo";
    public static final String PATH_LOCATION_PHOTO = "location/%s/photo";
    public static final String PATH_MESSAGES_ID = "messages/%s";
    public static final String PATH_ME_OFFERS= "me/offers";
    public static final String PATH_ME_OFFERS_ID= "me/offers/%s";
    public static final String PATH_ME_UPTO= "me/upto";
    public static final String PATH_ME_BUY= "me/buy";
    public static final String PATH_ME_REGISTER_DEVICE = "me/register_device";
    public static final String PATH_ME_UNREGISTER_DEVICE = "me/unregister_device";
    public static final String PATH_ME_CLEAR_NOTIFICATION = "me/clear_notification";

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

    // Shared

    public static final String NAME = "snappy";
    public static final String PACKAGE = "com.queatz.snappy";
    public static final String CLIENT_ID = "1098230558363-qe1do9mi41ptg644bd12m90sbba767e2.apps.googleusercontent.com";
    public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String QUEUE_WORKER_URL = "/worker";
    public static final String QUEUE_WORKER_NAME = "pushes";

    public static final boolean IN_BETA = false;
    public static final boolean PUBLIC_BUY = true;
    public static final int TEMPORARY_API_LIMIT = 100;

    public static final int SEARCH_DISTANCE = 1609 * 7;
    public static final int SEARCH_MINIMUM = 10;
    public static final int SEARCH_MAXIMUM = 30;
    public static final int SEARCH_MAX_VISIBILITY = 1609 * 300;
    public static final int NEARBY_MAX_VISIBILITY = 1609 * 2;
    public static final int BOUNTIES_MAX_AGE = 1000 * 60 * 60 * 24 * 7;
    public static final long QUESTS_MAX_AGE = 1000L * 60L * 60L * 24L * 30L;
    public static final int QUEST_MAX_TEAM_SIZE = 6;
    public static final int BOUNTIES_MAXIMUM = 100;
    public static final int QUESTS_MAXIMUM = 100;
    public static final int SUGGESTION_LIMIT = 5;
    public static final int SUGGESTION_MAX_DISTANCE = 1609 * 7;
    public static final int SEARCH_PEOPLE_MAX_NEAR_HERE = 500;
    public static final int SEARCH_PEOPLE_MAX_DISTANCE = 804;
    public static final int SEARCH_LOCATIONS_MAX_HERE = 10;
    public static final int OFFER_MAX_PRICE = 200;
    public static final int BOUNTY_MIN_PRICE = 500;
    public static final int BOUNTY_MAX_PRICE = 15000;

    public static final String HOSTING_BETATESTER = "betatester";

    public static final String HOSTING_ENABLED_FALSE = Boolean.toString(false);
    public static final String HOSTING_ENABLED_AVAILABLE = "available";
    public static final String HOSTING_ENABLED_TRUE = Boolean.toString(true);

    public static final String SOCIAL_MODE_OFF = "off";
    public static final String SOCIAL_MODE_FRIENDS = "friends";
    public static final String SOCIAL_MODE_ON = "on";

    public static final String PUSH_ACTION_JOIN_REQUEST = "join_requested";
    public static final String PUSH_ACTION_JOIN_ACCEPTED = "join_accepted";
    public static final String PUSH_ACTION_HOSTING_REMINDER = "hosting_reminder";
    public static final String PUSH_ACTION_FOLLOW = "follow";
    public static final String PUSH_ACTION_MESSAGE = "message";
    public static final String PUSH_ACTION_NEW_PARTY = "new_party";
    public static final String PUSH_ACTION_JOIN_PARTY = "join_party";
    public static final String PUSH_ACTION_REFRESH_ME = "refresh_me";
    public static final String PUSH_ACTION_CLEAR_NOTIFICATION = "clear_notification";
    public static final String PUSH_ACTION_BOUNTY_FINISHED = "bounty_finished";
    public static final String PUSH_ACTION_QUEST_COMPLETED = "quest_completed";
    public static final String PUSH_ACTION_QUEST_STARTED = "quest_started";
    public static final String PUSH_ACTION_NEW_UPTO = "new_upto";

    public static final String UPDATE_ACTION_JOIN_PARTY = "join_party";
    public static final String UPDATE_ACTION_HOST_PARTY = "host_party";
    public static final String UPDATE_ACTION_UPTO = "upto";

    public static final String JOIN_STATUS_REQUESTED = "requested";
    public static final String JOIN_STATUS_IN = "in";
    public static final String JOIN_STATUS_OUT = "out";
    public static final String JOIN_STATUS_WITHDRAWN = "withdrawn";

    public static final String BOUNTY_STATUS_OPEN = "open";
    public static final String BOUNTY_STATUS_CLAIMED = "claimed";
    public static final String BOUNTY_STATUS_FINISHED = "finished";

    public static final String QUEST_STATUS_OPEN = "open";
    public static final String QUEST_STATUS_STARTED = "started";
    public static final String QUEST_STATUS_COMPLETE = "complete";

    public static final String PARAM_ABOUT = "about";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_AUTH = "auth";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_CLAIM = "claim";
    public static final String PARAM_FINISH = "finish";
    public static final String PARAM_PHOTO = "photo";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DETAILS = "details";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_ID = "id";
    public static final String PARAM_JOIN = "join";
    public static final String PARAM_CANCEL_JOIN = "cancel_join";
    public static final String PARAM_FULL= "full";
    public static final String PARAM_ACCEPT = "accept";
    public static final String PARAM_HIDE = "hide";
    public static final String PARAM_FOLLOW = "follow";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";
    public static final String PARAM_SEEN = "seen";
    public static final String PARAM_SOCIAL_MODE = "social_mode";
    public static final String PARAM_LOCAL_ID = "local_id";
    public static final String PARAM_DEVICE_ID = "device_id";
    public static final String PARAM_PURCHASE_DATA = "purchase_data";
    public static final String PARAM_NOTIFICATION = "notification";
    public static final String PARAM_SIZE = "s";
    public static final String PARAM_PRICE = "price";
    public static final String PARAM_TIME = "time";
    public static final String PARAM_TEAM_SIZE = "team_size";
    public static final String PARAM_REWARD = "reward";
    public static final String PARAM_START = "start";
    public static final String PARAM_COMPLETE = "complete";

    public static final String GOOGLE_PLUS_PROFILE_URL = "https://www.googleapis.com/plus/v1/people/me";
    public static final String GOOGLE_PLUS_TOKENINFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";
    public static final String GOOGLE_BILLING_URL = "https://www.googleapis.com/androidpublisher/v2/applications/%s/purchases/subscriptions/%s/tokens/%s";
    public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GOOGLE_AUTH_URL_POST_PARAMS = "grant_type=refresh_token&refresh_token=%s&client_id=%s";
    public static final String subscriptionProductId = "com.queatz.snappy.monthly";
    public static final String refreshToken = "1/GBpu5P7H3RGjlaNnVgoKMqE-GrEBbLAJHjEPpFzK5I4";

    public static final String PATH_ABOUT = "about";
    public static final String PATH_BOUNTIES = "bounties";
    public static final String PATH_BOUNTY = "bounty";
    public static final String PATH_API = "api";
    public static final String PATH_PARTY = "party";
    public static final String PATH_PARTIES = "parties";
    public static final String PATH_QUEST = "quest";
    public static final String PATH_JOIN = "join";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_PEOPLE = "people";
    public static final String PATH_FOLLOW = "follow";
    public static final String PATH_LOCATIONS = "locations";
    public static final String PATH_LOCATION = "location";
    public static final String PATH_ME = "me";
    public static final String PATH_OFFERS = "offers";
    public static final String PATH_UPTO = "upto";
    public static final String PATH_HERE = "here";
    public static final String PATH_BUY = "buy";
    public static final String PATH_REGISTER_DEVICE = "register_device";
    public static final String PATH_UNREGISTER_DEVICE = "unregister_device";
    public static final String PATH_CLEAR_NOTIFICATION = "clear_notification";
    public static final String PATH_PHOTO = "photo";
    public static final String PATH_PIRATE = "yarr";
    public static final String PATH_ADMIN = "admin";
    public static final String PATH_FOLLOWERS = "followers";
    public static final String PATH_FOLLOWING = "following";
    public static final String PATH_UPDATE = "update";
}