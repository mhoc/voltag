package edu.purdue.voltag.data;

/** Class which stores static constants for object names and fields in the parse backend */
public abstract class ParseConstants {

    /** Parse keys please dont stealerino */
    public static final String PARSE_APPLICATION_KEY = "RP4GH0ySHN7O2LdJQxGAlFEKgfN5s4aM4gOmpGAs";
    public static final String PARSE_CLIENT_KEY = "pcNQ7PZCQv4gGWjCjR9ia6SVhNIetod2LNvb5ZhX";

    /** Object names */
    public static final String PARSE_CLASS_PLAYER = "Player";
    public static final String PARSE_CLASS_GAME = "Game";
    public static final String PARSE_CLASS_TAG = "Tag";

    /** General class columns */
    public static final String CLASS_ID = "objectId";
    public static final String CLASS_CREATED_AT = "createdAt";
    public static final String CLASS_UPDATED_AT = "updatedAt";
    public static final String CLASS_ACL = "ACL";

    /** Player class */
    public static final String PLAYER_HARDWARE_ID = "hardwareID";
    public static final String PLAYER_NAME = "name";
    public static final String PLAYER_EMAIL = "email";

    /** Game class */
    public static final String GAME_NAME = "name";
    public static final String GAME_TAGGED = "tagged";
    public static final String GAME_PLAYERS = "players";

    /** Tag class */
    public static final String TAG_GAME = "game";
    public static final String TAG_PLAYER_IT = "player_it";
    public static final String TAG_PLAYER_TAGGED = "player_tagged";

}
