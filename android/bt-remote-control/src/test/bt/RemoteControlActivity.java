package test.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

/**
 * Created by seth.yang on 2015/6/23.
 */
public class RemoteControlActivity extends Activity implements View.OnTouchListener, View.OnClickListener {
    private Peer peer;
    private BluetoothDevice device;

    @Override
    protected void onCreate (Bundle bundle) {
        super.onCreate (bundle);
        setContentView (R.layout.control_panel);
        try {
            findViewById (R.id.up).setOnTouchListener (this);
            findViewById (R.id.left).setOnTouchListener (this);
            findViewById (R.id.right).setOnTouchListener (this);
            findViewById (R.id.down).setOnTouchListener (this);

            findViewById (R.id.stop).setOnClickListener (this);
            findViewById (R.id.led).setOnClickListener (this);

            if (bundle != null) {
                device = bundle.getParcelable ("device");
            } else {
                Intent intent = getIntent ();
                device = intent.getParcelableExtra ("device");
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    @Override
    protected void onResume () {
        super.onResume ();
        if (device != null) {
            peer = new Peer (device);
            try {
                peer.connect ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    @Override
    protected void onPause () {
        super.onPause ();
        try {
            peer.disconnect ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        peer = null;
    }

    @Override
    protected void onSaveInstanceState (Bundle bundle) {
        super.onSaveInstanceState (bundle);
        if (device != null)
            bundle.putParcelable ("device", device);
    }

    @Override
    protected void onRestoreInstanceState (Bundle bundle) {
        super.onRestoreInstanceState (bundle);
        device = bundle.getParcelable ("device");
    }

    @Override
    public void onClick (View v) {
        try {
            switch (v.getId ()) {
                case R.id.stop:
                    peer.sendCommand (Command.STOP);
                    break;
                case R.id.led:
                    peer.sendCommand (Command.TOGGLE_LED);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    @Override
    public boolean onTouch (View v, MotionEvent event) {
        try {
            int action = event.getAction ();
            if (action == MotionEvent.ACTION_UP) {
                switch (v.getId ()) {
                    case R.id.left:
                        peer.sendCommand (Command.TURN_LEFT);
                        break;
                    case R.id.right :
                        peer.sendCommand (Command.TURN_RIGHT);
                        break;
                }
            } else if (action == MotionEvent.ACTION_DOWN) {
                switch (v.getId ()) {
                    case R.id.up:
                        peer.sendCommand (Command.FORWARD);
                        break;
                    case R.id.left:
                        peer.sendCommand (Command.TURN_LEFT);
                        break;
                    case R.id.right:
                        peer.sendCommand (Command.TURN_RIGHT);
                        break;
                    case R.id.down:
                        peer.sendCommand (Command.BACKWARD);
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        return event.getAction () == MotionEvent.ACTION_DOWN;
    }
}
