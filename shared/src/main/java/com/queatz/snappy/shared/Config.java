package com.queatz.snappy.shared;

import java.util.UUID;

/**
 * Created by jacob on 10/25/14.
 */
public class Config {

    // Village admin user id (i.e. who gets feedback emails)
    public static final String JACOB = "8393";

    public static final String LOCAL_IP = "192.168.1.114";

    // Android

    /**
     * Force this to use the beta configuration.
     */
    public static final boolean BETA_VERSION = false;

    /**
     * During app upgrades, this value is compared against the app version code, and if it is less
     * than this number, then the database will be wiped clean, and the new version code will be
     * stored.
     */
    public static final int WIPE_VERSIONS_BELOW = 47;

    // The email emails are sent from
    public static final String VILLAGE_EMAIL_ADDRESS = "my@village.city";
    public static final String VILLAGE_EMAIL = "Village <" + VILLAGE_EMAIL_ADDRESS + ">";
    public static final String VILLAGE_WEBSITE = "https://vlllage.com/";


    /**
     * The log tag for the app.
     */
    public static final String LOG_TAG = "SNAPPY_LOG";

    /**
     * The Village backend.
     */
    public static final String BACKEND_DOMAIN = (BETA_VERSION ? LOCAL_IP + ":8080" : "vlllage.com:8443");

    /**
     * The Village backend url.
     */
    public static final String BASE_URL = (BETA_VERSION ? "http://" : "https://") + BACKEND_DOMAIN;

    /**
     * The Village backend url.
     */
    public static final String API_URL = BASE_URL + "/api/";

    /**
     * The Village WebSocket url.
     */
    public static final String WS_URI = (BETA_VERSION ? "ws://" : "wss://") + BACKEND_DOMAIN + "/ws";

    /**
     * The url for queues, accessed by the backend.  Localhost if on the same machine.
     */
    public static final String QUEUE_URL = "http://127.0.0.1:8080/";

    /**
     * Google's url for places autocomplete.
     */
    public static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?location=%f,%f&radius=1609&input=%s&key=%s";

    /**
     * Google's url for place details.
     */
    public static final String GOOGLE_PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&key=%s";

    /**
     * The default number of times every request is tried.
     */
    public static final int maxRequestRetries = 4;

    /**
     * The maximum time in the future that a party can be set for.
     */
    public static final int maxHoursInFuture = 18;

    /**
     * The accuracy, in meters, which will trigger an update in location.
     */
    public static final float locationAccuracy = 100;

    /**
     * The initial zoom of the map.
     */
    public static final float defaultMapZoom = 18;

    /**
     * The maximum acuracy of users that is shared with other users of Village.
     */
    public static final double personLocationAccuracy = 0.0473485; // 250ft

    /**
     * The (hidden) default duration of a party.
     */
    public static final long PARTY_DEFAULT_DURATION = 1000 * 60 * 60;

    /**
     * The folder where images and files are persisted to disk.
     */
    public static final String VILLAGE_FILES_DIR = "/var/lib/village/";


    // Shared Preferences (Android)
    public static final String PREFERENCE_USER = "auth.user";
    public static final String PREFERENCE_AUTH_TOKEN = "auth.auth_token";
    public static final String PREFERENCE_GCM_REGISTRATION_ID = "auth.gcm_registration_id";
    public static final String PREFERENCE_HOST_PARTY_SCREEN_SHOWN = "other.party_screen_shown";
    public static final String PREFERENCE_HOSTING_ENABLED = "other.hosting_enabled";
    public static final String PREFERENCE_SOCIAL_MODE = "other.social_mode";
    public static final String PREFERENCE_APP_VERSION = "app.version";
    public static final String PREFERENCE_MAP_POSITION = "other.map.position";
    public static final String PREFERENCE_RECENT_MAIN_SCREEN = "app.main.screen.index";
    public static final String PREFERENCE_CHAT_TOKEN = "chat.token";
    public static final String PREFERENCE_CHAT_AVATAR = "chat.avatar";

    // Android request codes
    public static final int REQUEST_CODE_AUTH_RESOLUTION = 1;
    public static final int REQUEST_CODE_ACCOUNT_PICKER = 2;
    public static final int REQUEST_CODE_BUY_INTENT = 3;
    public static final int REQUEST_CODE_PLAY_SERVICES = 4;
    public static final int REQUEST_CODE_CHECK_SETTINGS = 5;
    public static final int REQUEST_CODE_CHOOSER = 6;
    public static final int REQUEST_CODE_ENABLE_BT = 7;
    public static final int REQUEST_CODE_REQUEST_PERMISSION = 8;
    public static final int REQUEST_CODE_SEND_CHAT_PHOTO = 9;

    // Bluetooth GATT configuration
    public static final UUID UUID_SERVICE = UUID.fromString("2d444574-5fdb-4e49-8921-3823f80b39a5");
    public static final UUID UUID_CHARACTERISTIC_PROFILE_ID = UUID.fromString("9592986d-0038-4ebf-acc2-6454f30e9db4");
    public static final UUID UUID_CHARACTERISTIC_PROFILE_FIRST_NAME = UUID.fromString("d233da61-49ee-4ec9-94f2-576d8e7dddb0");
    public static final int REPORTING_DELAY = 1000;
    public static final int ADVERTISE_TIMEOUT = 1000 * 60 * 2;
    public static final int ADVERTISE_HIDE_TIMEOUT = 1000 * 60 * 10;

    // Android intent extras
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_PARTY_ID = "party";
    public static final String EXTRA_JOIN_ID = "join";
    public static final String EXTRA_PERSON_ID = "person";
    public static final String EXTRA_ACTION_JOIN_ACCEPT = "join.accept";
    public static final String EXTRA_ACTION_JOIN_REQUEST = "join.request";
    public static final String EXTRA_ACTION_HIDE = "hide";
    public static final String EXTRA_SHOW = "show";

    // Various path parts the app will use to build requests to the backend
    public static final String PATH_QUEST_ID = "quest/%s";
    public static final String PATH_PARTY_ID = "party/%s";
    public static final String PATH_JOIN_ID = "join/%s";
    public static final String PATH_PEOPLE_ID = "people/%s";
    public static final String PATH_PEOPLE_FOLLOWING = "%s/following";
    public static final String PATH_PEOPLE_FOLLOWERS = "%s/followers";
    public static final String PATH_PEOPLE_PARTIES = "people/%s/parties";
    public static final String PATH_SEARCH = "search";
    public static final String PATH_UPDATE_ID = "update/%s";
    public static final String PATH_UPDATE_LIKERS = "update/%s/likers";
    public static final String PATH_EARTH_PHOTO = "earth/%s/photo";
    public static final String PATH_MESSAGES_ID = "messages/%s";
    public static final String PATH_OFFER_ID = "offer/%s";
    public static final String PATH_ME_BUY= "me/buy";
    public static final String PATH_ME_REGISTER_DEVICE = "me/register_device";
    public static final String PATH_ME_UNREGISTER_DEVICE = "me/unregister_device";
    public static final String PATH_ME_CLEAR_NOTIFICATION = "me/clear_notification";
    public static final String PATH_ABOUT = "about";
    public static final String PATH_API = "api";
    public static final String PATH_PARTY = "party";
    public static final String PATH_DELETE = "delete";
    public static final String PATH_PARTIES = "parties";
    public static final String PATH_QUEST = "quest";
    public static final String PATH_JOIN = "join";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_MESSAGE= "message";
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
    public static final String PATH_LIKERS = "likers";
    public static final String PATH_OFFER = "offer";
    public static final String PATH_BY_NAME = "by-name";
    public static final String PATH_EARTH = "earth";
    public static final String PATH_EDIT = "edit";
    public static final String PATH_LIKE = "like";
    public static final String PATH_REPORT = "report";
    public static final String PATH_INFO = "info";
    public static final String PATH_GEO_SUBSCRIBE = "geo-subscribe";
    public static final String PATH_RAW = "raw";
    public static final String PATH_CLUBS = "clubs";

    // Google Play billing configuration (unused)
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
    public static final String PROJECT_ID = Gateway.projectId;
    public static final String CLIENT_ID = Gateway.clientId;
    public static final String GCM_KEY = Gateway.gcmKey;
    public static final String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
    public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String QUEUE_WORKER_URL = QUEUE_URL + "worker";
    public static final String QUEUE_WORKER_NAME = "pushes";
    public static final String QUEUE_IMAGE_WORKER_NAME = "images";
    public static final String QUEUE_IMAGE_WORKER_URL = QUEUE_URL + "image-worker";

    // If the app is in a beta release period (unused)
    public static final boolean IN_BETA = false;
    public static final boolean PUBLIC_BUY = true;
    public static final int TEMPORARY_API_LIMIT = 100;

    // Configuration for monthly subscriptions (unused)
    public static final int FREE_OFFER_PRICE_MIN = -100;
    public static final int FREE_OFFER_PRICE_MAX = 200;
    public static final int PAID_OFFER_PRICE_MIN = -1000;
    public static final int PAID_OFFER_PRICE_MAX = 1000;

    // Proximity search configuration
    public static final int SEARCH_DISTANCE = 1609 * 7;
    public static final int SEARCH_MINIMUM = 10;
    public static final int SEARCH_MAXIMUM = 10;
    public static final int SEARCH_MAX_VISIBILITY = 1609 * 20;
    public static final int NEARBY_MAX_VISIBILITY = 1609 * 2;
    public static final int NEARBY_MAX_COUNT = 50;
    public static final long QUESTS_MAX_AGE = 1000L * 60L * 60L * 24L * 30L;
    public static final int QUEST_MAX_TEAM_SIZE = 6;
    public static final int QUESTS_MAXIMUM = 100;
    public static final int SUGGESTION_LIMIT = 5;
    public static final int SUGGESTION_MAX_DISTANCE = 1609 * 7;
    public static final int SEARCH_PEOPLE_MAX_NEAR_HERE = 500;
    public static final int SEARCH_HERE_MAX_DISTANCE = 1609 * 2;
    public static final int SEARCH_LOCATIONS_MAX_HERE = 10;
    public static final int MAX_IDLE_HOURS = 1800;

    // The subscription values (unused)
    public static final String HOSTING_BETATESTER = "betatester";
    public static final String HOSTING_ENABLED_FALSE = Boolean.toString(false);
    public static final String HOSTING_ENABLED_AVAILABLE = "available";
    public static final String HOSTING_ENABLED_TRUE = Boolean.toString(true);

    // The different values of modes
    public static final String SOCIAL_MODE_OFF = "off";
    public static final String SOCIAL_MODE_FRIENDS = "friends";
    public static final String SOCIAL_MODE_ON = "on";

    // The different types of push notifications
    public static final String PUSH_ACTION_JOIN_REQUEST = "join_requested";
    public static final String PUSH_ACTION_JOIN_ACCEPTED = "join_accepted";
    public static final String PUSH_ACTION_HOSTING_REMINDER = "hosting_reminder";
    public static final String PUSH_ACTION_FOLLOW = "follow";
    public static final String PUSH_ACTION_MESSAGE = "message";
    public static final String PUSH_ACTION_NEW_PARTY = "new_party";
    public static final String PUSH_ACTION_JOIN_PARTY = "join_party";
    public static final String PUSH_ACTION_REFRESH_ME = "refresh_me";
    public static final String PUSH_ACTION_CLEAR_NOTIFICATION = "clear_notification";
    public static final String PUSH_ACTION_QUEST_COMPLETED = "quest_completed";
    public static final String PUSH_ACTION_QUEST_STARTED = "quest_started";
    public static final String PUSH_ACTION_NEW_UPTO = "new_upto";
    public static final String PUSH_ACTION_NEW_OFFER = "new_offer";
    public static final String PUSH_ACTION_ADVERTISE = "advertise";
    public static final String PUSH_ACTION_LIKE_UPDATE = "like_update";
    public static final String PUSH_ACTION_NEW_THING = "new_thing";
    public static final String PUSH_ACTION_NEW_CONTACT = "new_contact";
    public static final String PUSH_ACTION_OFFER_LIKED = "offer_liked";
    public static final String PUSH_ACTION_NEW_COMMENT = "new_comment";
    public static final String PUSH_ACTION_INFORMATION = "information";
    public static final String PUSH_ACTION_FORM_SUBMISSION_EVENT = "form_submission";
    public static final String PUSH_ACTION_NEW_CHAT = "chat_new";

    // Types of updates
    public static final String UPDATE_ACTION_JOIN_PARTY = "join_party";
    public static final String UPDATE_ACTION_HOST_PARTY = "host_party";
    public static final String UPDATE_ACTION_UPTO = "upto";

    // Status of join requests (for parties)
    public static final String JOIN_STATUS_REQUESTED = "requested";
    public static final String JOIN_STATUS_IN = "in";
    public static final String JOIN_STATUS_OUT = "out";
    public static final String JOIN_STATUS_WITHDRAWN = "withdrawn";

    // Member statuses
    public static final String MEMBER_STATUS_SUGGESTED = "suggested";
    public static final String MEMBER_STATUS_ACTIVE = "active";

    // Quest statuses
    public static final String QUEST_STATUS_OPEN = "open";
    public static final String QUEST_STATUS_STARTED = "started";
    public static final String QUEST_STATUS_COMPLETE = "complete";

    // Backend url parameters
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
    public static final String PARAM_UNIT = "unit";
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
    public static final String PARAM_LIKE = "like";
    public static final String PARAM_THING = "thing";
    public static final String PARAM_EDIT = "edit";
    public static final String PARAM_FEEDBACK = "feedback";
    public static final String PARAM_ROLE = "role";
    public static final String PARAM_PERSON = "person";
    public static final String PARAM_KIND = "kind";
    public static final String PARAM_Q = "q";
    public static final String PARAM_RECENT = "recent";
    public static final String PARAM_SOURCE = "source";
    public static final String PARAM_TARGET = "target";
    public static final String PARAM_IN = "in";
    public static final String PARAM_UNSUBSCRIBE = "unsubscribe";
    public static final String PARAM_WITH = "with";
    public static final String PARAM_GOING = "going";
    public static final String PARAM_WANT = "want";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_HIDDEN = "hidden";
    public static final String PARAM_CLUBS = "clubs";

    // Google login configuration
    public static final String GOOGLE_PLUS_PROFILE_URL = "https://www.googleapis.com/plus/v1/people/me";
    public static final String GOOGLE_PLUS_TOKENINFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";
    public static final String GOOGLE_BILLING_URL = "https://www.googleapis.com/androidpublisher/v2/applications/%s/purchases/subscriptions/%s/tokens/%s";
    public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GOOGLE_AUTH_URL_POST_PARAMS = "grant_type=refresh_token&refresh_token=%s&client_id=%s";
    public static final String subscriptionProductId = "com.queatz.snappy.monthly";
    public static final String refreshToken = Gateway.refreshToken;

    public static final boolean REQUIRE_BLUETOOTH = false;
}