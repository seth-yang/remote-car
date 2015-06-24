package org.dreamwork.smart.home.remote.wifi;

/**
 * Created by seth.yang on 2015/6/14.
 */
public interface BroadcastListener {
    void onReceive (byte[] buff);
}