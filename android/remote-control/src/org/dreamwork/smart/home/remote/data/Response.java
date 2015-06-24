package org.dreamwork.smart.home.remote.data;

/**
 */
public abstract class Response<T> {
    protected int status;
    protected int responseType;
    protected byte[] data;
    protected T value;

    public static final int TYPE_INTEGER = 0,
                            TYPE_STRING = 1;

    protected Response (int status, int responseType, byte[] data) {
        this.status = status;
        this.responseType = responseType;
        setData (data);
    }

    public int getStatus () {
        return status;
    }

    public void setStatus (int status) {
        this.status = status;
    }

    public int getResponseType () {
        return responseType;
    }

    public void setResponseType (int responseType) {
        this.responseType = responseType;
    }

    public T getValue () {
        return value;
    }

    public byte[] getData () {
        return data;
    }

    public void setData (byte[] data) {
        this.data = data;
        parseData (data);
    }

    protected abstract void parseData (byte[] data);

    public static Response parseResponse (int status, int responseType, byte[] data) {
        switch (responseType) {
            case TYPE_INTEGER :
                return new IntegerResponse (status, data);
            case TYPE_STRING :
                return new StringResponse (status, data);
        }

        throw new IllegalArgumentException ("Unknown type: " + responseType);
    }
}