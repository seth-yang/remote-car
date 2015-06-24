package org.dreamwork.smart.home.remote.data;

import java.io.UnsupportedEncodingException;

public class StringResponse extends Response {

    protected StringResponse (int status, byte[] data) {
        super (status, TYPE_STRING, data);
    }

    @Override
    protected void parseData (byte[] data) {
        try {
            value = new String (data, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace ();
        }
    }
}