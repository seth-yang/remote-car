package org.dreamwork.smart.home.remote;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 15-3-6
 * Time: 下午10:35
 */
public class MjpgVideoView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder holder;
    private String url;
    private boolean running = true;
    private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object> (32);
    private Executor service = Executors.newCachedThreadPool ();
    private MjpgDownloader downloader;
    private Timer timer;

    private int width, height, frames, fps;

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public MjpgVideoView (Context context, AttributeSet attrs) {
        super (context, attrs);
        this.setKeepScreenOn(true);
        holder = this.getHolder();
        holder.addCallback (this);
        setFocusable (true);
    }

    @Override
    public void surfaceCreated (SurfaceHolder holder) {
        Log.d (Const.TAG, "surface created.");
        service.execute (this);
        width = getMeasuredWidth ();
        height = getMeasuredHeight ();
    }

    @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {
        Log.d (Const.TAG, "surface changed. format = " + format + ", width = " + width + ", height = " + height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder) {
        Log.d (Const.TAG, "surface destoryed.");
        Log.d (Const.TAG, "shutting down the update thread.");
        running = false;
    }

    public void start () throws MalformedURLException {
        downloader = new MjpgDownloader (queue, new URL (url));
        service.execute (downloader);
        timer = new Timer ();
        timer.scheduleAtFixedRate (new TimerTask () {
            @Override
            public void run () {
                synchronized (MjpgVideoView.this) {
                    fps = frames;
                    frames = 0;
                }
            }
        }, 1000, 1000);
        new Thread (this).start ();
    }

    public void pause () {
        if (downloader != null) {
            downloader.cancel ();
        }
    }

    public void shutdown () {
        if (timer != null) {
            timer.cancel ();
        }
        pause ();
        synchronized (this) {
            running = false;
        }
    }

    public synchronized boolean isRunning () {
        return running;
    }

    public int getMaxWidth () {
        return width;
    }

    public int getMaxHeight () {
        return height;
    }

    @Override
    public void run () {
        final int TEXT_SIZE = 25;
        Paint pt = new Paint();
        pt.setAntiAlias(true);
        pt.setColor(Color.GREEN);
        pt.setTextSize(TEXT_SIZE);
        pt.setStrokeWidth(1);
        pt.setTypeface(Typeface.create (Typeface.MONOSPACE, Typeface.NORMAL));

        while (running) {
            try {
                Object obj = queue.take ();
                if (obj instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) obj;
                    synchronized (this) {
                        frames ++;
                    }
                    double w = bitmap.getWidth (), h = bitmap.getHeight ();
                    double sw = w / width, sh = h / height, scale = Math.min (sw, sh);
                    int pw = (int) (w / scale), ph = (int) (h / scale);
                    bitmap = Bitmap.createScaledBitmap (bitmap, pw, ph, false);
                    Canvas canvas = holder.lockCanvas ();
                    if (canvas != null) {
                        canvas.drawColor (Color.BLACK);
                        canvas.drawBitmap (bitmap, (width - pw) / 2, (height - ph) / 2, null);
                        canvas.drawText ("fps: " + fps, 10, TEXT_SIZE + 10, pt);
                        holder.unlockCanvasAndPost (canvas);
                    }
                } else {
                    break;
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace ();
            }
        }

        Log.d (Const.TAG, "update thread stopped. clear all resource");
        queue.clear ();
    }
}