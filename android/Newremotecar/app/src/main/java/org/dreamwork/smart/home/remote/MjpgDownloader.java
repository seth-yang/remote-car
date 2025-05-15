package org.dreamwork.smart.home.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 15-3-6
 * Time: 下午10:26
 */
public class MjpgDownloader implements Runnable {
    private BlockingQueue<Object> queue;
    private URL url;
    private boolean running = true;
    private final Object locker = new Object ();

    public MjpgDownloader (BlockingQueue<Object> queue, URL url) {
        this.queue = queue;
        this.url = url;
    }

    @Override
    public void run () {
        HttpURLConnection conn = null;
        for (int i = 0; i < 3; i ++) {
            try {
                Log.d (Const.TAG, "opening url: " + url);
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
                        Bitmap bmp = BitmapFactory.decodeStream (new ByteArrayInputStream (baos.toByteArray (), 0, baos.size ()));
                        try {
                            queue.offer (bmp, 1, TimeUnit.SECONDS);
                        } catch (InterruptedException ex) {
                            Log.w (Const.TAG, ex.getMessage (), ex);
                        }
                    }
                }

                return;
            } catch (IOException ex) {
                Log.e (Const.TAG, ex.getMessage (), ex);
                try {
                    Thread.sleep (2000);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            } finally {
                if (conn != null) {
                    conn.disconnect ();
                }
            }
        }
    }

    public void cancel () {
        running = false;
        queue.offer ("Shutdown");
        Log.d (Const.TAG, "download cancel");
    }
}