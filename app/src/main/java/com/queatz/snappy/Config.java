package com.queatz.snappy;

/**
 * Created by jacob on 10/25/14.
 */
public class Config {
    public static String LOG_TAG = "SNAPPY_LOG";
    public static String API_URL = "http://queatz-snappy.appspot.com/api";
    public static int maxHoursInFuture = 18;
    public static float locationAccuracy = 50;

    public static final String PREFERENCE_USER = "auth.user";
    public static final String PREFERENCE_AUTH_TOKEN = "auth.auth_token";

    public static final int REQUEST_CODE_AUTH_RESOLUTION = 1;
    public static final int REQUEST_CODE_ACCOUNT_PICKER = 2;

    public static String PARAM_AUTH = "auth";
    public static String PARAM_EMAIL = "email";
    public static String PARAM_JOIN = "join";
    public static String PARAM_FULL = "full";
    public static String PARAM_ACCEPT = "accept";
    public static String PARAM_FOLLOW = "follow";
    public static String PARAM_MESSAGE = "message";

    public static String PATH_API = "api";
    public static String PATH_PARTIES = "parties";
    public static String PATH_PARTY = "party";
    public static String PATH_PARTY_ID = "party/%s";
    public static String PATH_JOIN_ID = "join/%s";
    public static String PATH_PEOPLE_ID = "people/%s";
    public static String PATH_PEOPLE = "people";
    public static String PATH_SEARCH = "search";
    public static String PATH_MESSAGES = "messages";
    public static String PATH_UPTO = "upto";
    public static String PATH_ME= "me";
    public static String PATH_ME_UPTO= "me/upto";

    public static String JOIN_STATUS_REQUESTED = "requested";
    public static String JOIN_STATUS_IN = "in";
    public static String JOIN_STATUS_OUT = "out";
}
