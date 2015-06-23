package org.dreamwork.smart.car.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
* Created by seth.yang on 2015/6/14.
*/
public class VideoWorker implements Runnable {
    private BlockingQueue<BufferedImage> queue;
    private URL url;
    private boolean running = true;

    public VideoWorker (BlockingQueue<BufferedImage> queue, URL url) {
        this.queue = queue;
        this.url = url;
    }

    @Override
    public void run () {
        HttpURLConnection conn = null;
        try {
            System.out.println ("open url: " + url);
            conn = (HttpURLConnection) url.openConnection ();
            InputStream in = conn.getInputStream ();
            int imageSize = -1, pos;
            byte[] line;
            String text;

            while (running && (line = IOHelper.readLine (in)) != null) {
                text = new String (line, "utf-8").trim ();
                if (text.contains ("Content-Length:")) {
                    pos = text.indexOf (":");
                    imageSize = Integer.parseInt (text.substring (pos + 1).trim ());
                } else if (text.trim ().length () == 0 && imageSize > 0) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream (imageSize);
                    IOHelper.readMaxSize (in, baos, imageSize);
                    imageSize = -1;

                    ByteArrayInputStream buff = new ByteArrayInputStream (baos.toByteArray ());
                    BufferedImage image = ImageIO.read (buff);
                    try {
                        queue.offer (image, 1, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace ();
                    }
                }
            }

            synchronized (this) {
                notifyAll ();
            }
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            if (conn != null) {
                conn.disconnect ();
            }
        }
    }

    public void cancel () {
        running = false;
        synchronized (this) {
            try {
                wait (30000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }

        System.out.println ("work canceled.");
    }
}
