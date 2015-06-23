package org.dreamwork.smart.car.server.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth.yang on 2015/6/11.
 */
public class TimeoutMonitor extends Thread  {
    private long timestamp, timeout = 60000;
    private boolean running = true;
    private List<TimeoutListener> list = new ArrayList<TimeoutListener> ();

    private final Object locker = new Object ();

    public TimeoutMonitor (long timeout) {
        this.timeout = timeout;
    }

    public void addTimeoutListener (TimeoutListener listener) {
        list.add (listener);
    }

    @Override
    public void run () {
        while (running) {
            synchronized (locker) {
                if (System.currentTimeMillis () - timestamp > timeout) {
                    fireListener ();
                }
            }
            try {
                sleep (10);
            } catch (InterruptedException ex) {
                ex.printStackTrace ();
            }
        }
    }

    public void touch () {
        synchronized (locker) {
            timestamp = System.currentTimeMillis ();
        }
    }

    public void shutdown () {
        running = false;
    }

    protected void fireListener () {
        for (TimeoutListener listener : list) {
            listener.onTimeout ();
        }
    }
}