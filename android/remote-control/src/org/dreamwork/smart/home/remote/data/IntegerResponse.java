package org.dreamwork.smart.home.remote.data;

public class IntegerResponse extends Response<Integer> {
    public IntegerResponse (int status, byte[] data) {
        super (status, TYPE_INTEGER, data);
        setData (data);
    }

    @Override
    protected void parseData (byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        value = 0;
        for (int i = data.length - 1, bits = 0; i >= 0; i --, bits += 8) {
            value += (data [i] & 0xff) << bits;
        }
    }
}