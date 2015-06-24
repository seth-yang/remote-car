package org.dreamwork.smart.home.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.dreamwork.smart.home.remote.io.Command;
import org.dreamwork.smart.home.remote.io.Commander;
import org.dreamwork.smart.home.remote.io.ConnectionListener;

import java.io.IOException;
import java.net.MalformedURLException;

public class ControlPanel extends Activity implements View.OnTouchListener, View.OnClickListener, ConnectionListener {
    private static final String TAG = "sh-rc";
    private Commander commander;
    private MjpgVideoView surface;

    private String ip;
    private int controlPort, cameraPort;
    private long touch;
    private LinearLayout control_panel;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.main);

        findViewById (R.id.up).setOnTouchListener (this);
        findViewById (R.id.left).setOnTouchListener (this);
        findViewById (R.id.right).setOnTouchListener (this);
        findViewById (R.id.down).setOnTouchListener (this);

        findViewById (R.id.stop).setOnClickListener (this);
        findViewById (R.id.cam).setOnClickListener (this);
        findViewById (R.id.led).setOnClickListener (this);
        findViewById (R.id.setup).setOnClickListener (this);

        surface = (MjpgVideoView) findViewById (R.id.surface);
        surface.setVisibility (View.INVISIBLE);

        control_panel = (LinearLayout) findViewById (R.id.control_panel);
        control_panel.setVisibility (View.INVISIBLE);

        if (savedInstanceState != null) {
            ip = savedInstanceState.getString ("ip");
            controlPort = savedInstanceState.getInt ("control");
            cameraPort = savedInstanceState.getInt ("camera");
        } else {
            Intent intent = getIntent ();
            ip = intent.getStringExtra ("ip");
            controlPort = intent.getIntExtra ("control", -1);
            cameraPort = intent.getIntExtra ("camera", -1);
        }
    }

    private void forward () throws InterruptedException {
        Log.d (TAG, "forward");
        commander.sendCommand (Command.FORWARD);
    }

    private void backward () throws InterruptedException {
        Log.d (TAG, "backward");
        commander.sendCommand (Command.BACKWARD);
    }

    private void toggleTurnLeft () throws InterruptedException {
        Log.d (TAG, "turn left");
        commander.sendCommand (Command.TURN_LEFT);
    }

    private void toggleTurnRight () throws InterruptedException {
        Log.d (TAG, "turn right");
        commander.sendCommand (Command.TURN_RIGHT);
    }

    private void stop () throws InterruptedException {
        Log.d (TAG, "stop");
        commander.sendCommand (Command.STOP);
    }

    private void toggleLed () throws InterruptedException {
        Log.d (TAG, "toggle led");
        commander.sendCommand (Command.TOGGLE_LEFT_BLINK);
    }

    private void toggleCam () throws InterruptedException, MalformedURLException {
        Log.d (TAG, "toggle camera");
        commander.sendCommand (Command.TOGGLE_CAMERA);
        if (surface.getVisibility () == View.INVISIBLE) {
            surface.setVisibility (View.VISIBLE);
            surface.setUrl ("http://" + ip + ":" + cameraPort + "/?action=stream");
            surface.start ();
        } else {
            surface.pause ();
            surface.setVisibility (View.INVISIBLE);
        }
    }

    private void setup () {
        Log.d (TAG, "setup");
    }

    @Override
    public boolean onTouch (View v, MotionEvent event) {
        try {
            int action = event.getAction ();
            if (action == MotionEvent.ACTION_UP) {
                switch (v.getId ()) {
                    case R.id.left:
                        toggleTurnLeft ();
                        break;
                    case R.id.right :
                        toggleTurnRight ();
                        break;
                }
            } else if (action == MotionEvent.ACTION_DOWN) {
                switch (v.getId ()) {
                    case R.id.up:
                        forward ();
                        break;
                    case R.id.left:
                        toggleTurnLeft ();
                        break;
                    case R.id.right:
                        toggleTurnRight ();
                        break;
                    case R.id.down:
                        backward ();
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        return event.getAction () == MotionEvent.ACTION_DOWN;
    }

    @Override
    public void onClick (View v) {
        try {
            switch (v.getId ()) {
                case R.id.stop:
                    stop ();
                    break;
                case R.id.led:
                    toggleLed ();
                    break;
                case R.id.cam:
                    toggleCam ();
                    break;
                case R.id.setup:
                    setup ();
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        if (ip != null && controlPort != -1) try {
            commander = new Commander (ip, controlPort, this);
            commander.start ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            commander.sendCommand (Command.QUIT);
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState (outState);
        outState.putString ("ip", ip);
        outState.putInt ("control", controlPort);
        outState.putInt ("camera", cameraPort);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        ip = savedInstanceState.getString ("ip");
        controlPort = savedInstanceState.getInt ("control");
        cameraPort = savedInstanceState.getInt ("camera");
    }

    @Override
    public void onConnect () {
        control_panel.setVisibility (View.VISIBLE);
    }

    @Override
    public void onBackPressed () {
            // confirm quit.
            long now = System.currentTimeMillis ();
            if (now - touch > 2000) {
                Toast.makeText (this, "再按一次退出程序", Toast.LENGTH_LONG).show ();
                touch = now;
            } else {
                finish ();
                System.exit (0);
            }
    }
}