package org.dreamwork.smart.home.remote.io;

import org.dreamwork.smart.home.remote.data.Response;

/**
 */
public interface CommandListener {
    void onResponse (Response<?> response);
}