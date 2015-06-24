package org.dreamwork.smart.home.remote;

/**
 */
public interface Const {
    String TAG = "sh-rc";

    public static final int SOCKET_TIMEOUT = 10000;
    public static final int MAX_WAIT = 30000;
    public static final int RETRY_TIMES = 5;
    public static final int MESSAGE_TYPE_SUCCESS = 0;
    public static final int MESSAGE_TYPE_ERROR = 1;

    public static final String FILTER_BACKGROUND_SERVICE = "org.dreamwork.smart.home.filter.bgs";

    public static final String ACTION_BACKGROUND_SERVICE = "org.dreamwork.smart.home.bgs";

    public static final int ACTION_FIND_SERVER = 0x0001;
    public static final int ACTION_SEND_COMMAND = 0x0002;
    public static final int ACTION_STATUS = 0x0003;

    public static final String KEY_ACTION = "key.action";
    public static final String KEY_STATUS = "org.dreamwork.smart.home.bgs.status";
    public static final String KEY_ERROR_MESSAGE = "org.dreamwork.smart.home.bgs.em";
    public static final String KEY_SERVER_FOUND = "server-found";
    public static final String KEY_SHARED_REFERENCE_INSTANCE = "org.dreamwork.smart.home.sf";
    public static final String KEY_REMOTE_COMMAND = "remote-command";
    public static final String KEY_SERVER_ADDRESS = "remote.server.address";
    public static final String KEY_SERVER_PORT = "remote.server.ip";

    public static final int STATUS_OK = 0;
    public static final int STATUS_FAIL = -1;

    public static final boolean DEBUG = true;
}