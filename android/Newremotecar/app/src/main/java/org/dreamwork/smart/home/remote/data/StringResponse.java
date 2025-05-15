package org.dreamwork.smart.home.remote.data;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StringResponse extends Response<String> {

    protected StringResponse (int status, byte[] data) {
        super (status, TYPE_STRING, data);
    }

    @Override
    protected void parseData (byte[] data) {
        value = new String (data, StandardCharsets.UTF_8);
    }
}