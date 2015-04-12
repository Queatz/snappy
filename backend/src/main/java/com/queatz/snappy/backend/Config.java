package com.queatz.snappy.backend;

public class Config {
    public static final String NAME = "snappy";
    public static final String PACKAGE = "com.queatz.snappy";
    public static final String CLIENT_ID = "1098230558363-qe1do9mi41ptg644bd12m90sbba767e2.apps.googleusercontent.com";
    public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String QUEUE_WORKER_URL = "/worker";
    public static final String QUEUE_WORKER_NAME = "pushes";

    public static final int SEARCH_DISTANCE = 1609 * 7;

    public static final String PUSH_ACTION_JOIN_REQUEST = "join_requested";
    public static final String PUSH_ACTION_JOIN_ACCEPTED = "join_accepted";
    public static final String PUSH_ACTION_HOSTING_REMINDER = "hosting_reminder";
    public static final String PUSH_ACTION_FOLLOW = "follow";
    public static final String PUSH_ACTION_MESSAGE = "message";
    public static final String PUSH_ACTION_NEW_PARTY = "new_party";

    public static final String JOIN_STATUS_REQUESTED = "requested";
    public static final String JOIN_STATUS_IN = "in";
    public static final String JOIN_STATUS_OUT = "out";
    public static final String JOIN_STATUS_WITHDRAWN = "withdrawn";

    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_AUTH = "auth";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_ADDRESS = "address";
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
    public static final String PARAM_LOCAL_ID = "local_id";
    public static final String PARAM_DEVICE_ID = "device_id";
    public static final String PARAM_PURCHASE_DATA = "purchase_data";

    public static final String GOOGLE_PLUS_PROFILE_URL = "https://www.googleapis.com/plus/v1/people/me";
    public static final String GOOGLE_PLUS_TOKENINFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";
    public static final String GOOGLE_BILLING_URL = "https://www.googleapis.com/androidpublisher/v2/applications/%s/purchases/subscriptions/%s/tokens/%s";
    public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GOOGLE_AUTH_URL_POST_PARAMS = "grant_type=refresh_token&refresh_token=%s&client_id=%s";
    public static final String subscriptionProductId = "com.queatz.snappy.monthly";
    public static final String refreshToken = "1/GBpu5P7H3RGjlaNnVgoKMqE-GrEBbLAJHjEPpFzK5I4";

    public static final String PATH_API = "api";
    public static final String PATH_PARTY = "party";
    public static final String PATH_PARTIES = "parties";
    public static final String PATH_JOIN = "join";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_PEOPLE = "people";
    public static final String PATH_FOLLOW = "follow";
    public static final String PATH_ME = "me";
    public static final String PATH_BUY = "buy";
    public static final String PATH_REGISTER_DEVICE = "register_device";
    public static final String PATH_UNREGISTER_DEVICE = "unregister_device";
    public static final String PATH_PHOTO = "photo";
    public static final String PATH_PIRATE = "yarr";
    public static final String PATH_ADMIN = "admin";
}