package org.dreamwork.smart.car.server.util;

/**
 * 可暂停的线程
 * Created by seth.yang on 2015/6/10.
 */
public abstract class PausableThread extends Thread {
    protected final Object locker = new Object ();
    protected boolean paused = true;
    protected boolean running = false;

    protected abstract void doWork ();

    public PausableThread (boolean paused) {
        this.paused = paused;
        start ();
    }

    public void proceed () {
        synchronized (locker) {
            paused = false;
            locker.notifyAll ();
        }
    }

    public void pause () {
        paused = true;
    }

    public boolean isPaused () {
        return paused;
    }

    public void shutdown (boolean block) throws InterruptedException {
        running = false;
        if (paused)
            proceed ();
        if (block && (Thread.currentThread () != this))
            this.join ();
    }

    @Override
    public synchronized void start () {
        if (!running) {
            running = true;
            super.start ();
        }
    }

    @Override
    public void run () {
        while (running) {
            while (paused) {
                synchronized (locker) {
                    try {
                        locker.wait ();
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }

            doWork ();
        }
    }
}