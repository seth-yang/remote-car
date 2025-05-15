package org.dreamwork.smart.home.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import org.dreamwork.smart.home.remote.wifi.RemoteCar;

import java.io.IOException;

/**
 * Created by seth.yang on 2015/6/15.
 */
public class Splash extends Activity implements Runnable {
    private Handler handler;
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.splash);

        handler = new Handler ();
        new Thread (this).start ();
    }


    @Override
    public void run () {
        try {
            for (int i = 0; i < 3; i ++) {
                final RemoteCar car = RemoteCar.find (8001);
                if (car != null) {
                    handler.post (new Runnable () {
                        @Override
                        public void run () {
                            Intent intent = new Intent (Splash.this, ControlPanel.class);
                            intent.putExtra ("ip", car.getIp ().getHostAddress ());
                            intent.putExtra ("control", car.getControlPort ());
                            intent.putExtra ("camera", car.getCameraPort ());
                            startActivity (intent);
                            finish ();
                        }
                    });
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }
}