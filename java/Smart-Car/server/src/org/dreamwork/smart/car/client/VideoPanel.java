package org.dreamwork.smart.car.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
* Created by seth.yang on 2015/6/14.
*/
public class VideoPanel extends Canvas implements Runnable {
    private boolean running = true;
    private BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<BufferedImage> (32);
    private VideoWorker worker;
    private Thread downloadThread, updateThread;
    private URL url;
    private java.util.Timer timer;
    private static final Dimension size = new Dimension (640, 480);

    private int fps = 0, frames = 0;

    public VideoPanel (InetAddress ip, int port) {
        try {
            timer = new java.util.Timer ();
            url = new URL ("http://" + ip.getHostAddress () + ':' + port + "/?action=stream");
        } catch (MalformedURLException e) {
            e.printStackTrace ();
        }
    }

    @Override
    public Dimension getPreferredSize () {
        return size;
    }

    public void connect () throws MalformedURLException {
        worker = new VideoWorker (queue, url);
        downloadThread = new Thread (worker);
        downloadThread.start ();

        updateThread = new Thread (this);
        updateThread.start ();

        timer.schedule (new TimerTask () {
            @Override
            public void run () {
                synchronized (VideoPanel.class) {
                    fps = frames;
                    frames = 0;
                }
            }
        }, 0, 1000);

        System.out.println ("connected.");
    }

    public void disconnect () {
        if (downloadThread != null) {
            worker.cancel ();
            worker = null;
            System.out.println ("stopping all monitors.");
            downloadThread.interrupt ();
            downloadThread = null;
            updateThread.interrupt ();
            updateThread = null;
            running = false;
            timer.cancel ();
            System.out.println ("disconnect.");
        } else {
            System.out.println ("not at work.");
        }
    }

    public void play () {
        if (downloadThread == null) {
            worker = new VideoWorker (queue, url);
            downloadThread = new Thread (worker);
            downloadThread.start ();

            updateThread = new Thread (this);
            updateThread.start ();

            System.out.println ("play video");
        }
    }

    public void pause () {
        System.out.println ("pause video");
    }

    @Override
    public void run () {
        Graphics2D g = (Graphics2D) getGraphics ();
//        this.setDoubleBuffered (true);
        long timestamp, total, d, m, n;
        long fpsTime = (long) (1000d / 15 * 1000000);
        g.setColor (Color.GREEN);
        Font font = new Font ("DialogInput", Font.PLAIN, 24);
        g.setFont (font);
        while (running) {
            try {
                timestamp = System.nanoTime ();
                BufferedImage image = queue.take ();
                if (image != null) {
                    synchronized (this) {
                        frames ++;
                    }
                    g.setColor (Color.BLACK);
                    g.fillRect (0, 0, size.width, size.height);

                    g.drawImage (image, 0, 0, null);
                    g.drawString ("fps: " + fps, 40, 40);
                }
/*
                System.out.println (System.currentTimeMillis () - timestamp);
                timestamp = System.currentTimeMillis ();
*/
                total = System.nanoTime () - timestamp;
                if (total > fpsTime) continue;
                d = total - fpsTime;
                m = d / 1000;
                n = d % 1000;
                Thread.sleep (m, (int) n);

                while ((System.nanoTime () - timestamp) < fpsTime) {
                    System.nanoTime ();
                }
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
        }
        System.out.println ("stopped!");
    }
}
