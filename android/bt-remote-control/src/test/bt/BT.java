package test.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.util.*;

public class BT extends Activity implements AdapterView.OnItemClickListener {
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String TAG = "org.dreamwork.bluetooth.test";

    private BluetoothReceiver receiver;
    private BluetoothAdapter ba;
    private Set<String> keys = new HashSet<String> ();

    private BluetoothDeviceListAdapter adapter;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.main);

        adapter = new BluetoothDeviceListAdapter (this);
        ListView listView = (ListView) findViewById (R.id.listView);
        listView.setAdapter (adapter);
        listView.setOnItemClickListener (this);
    }

    @Override
    protected void onResume () {
        super.onResume ();
        IntentFilter filter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
        receiver = new BluetoothReceiver ();
        registerReceiver (receiver, filter);
        search ();
    }

    @Override
    protected void onPause () {
        super.onPause ();
        unregisterReceiver (receiver);
        if (ba != null) {
            ba.cancelDiscovery ();
        }
    }

    private void search() {
        ba = BluetoothAdapter.getDefaultAdapter();
        if (!ba.isEnabled()) {
            ba.enable();
        }
        ba.startDiscovery ();
        Log.d (TAG, "discovery started.");
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = adapter.getItem (position);
        Log.d (TAG, "you have choose device: " + device);
        ba.cancelDiscovery ();
        Intent intent = new Intent (this, RemoteControlActivity.class);
        intent.putExtra ("device", device);
        try {
            startActivity (intent);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        finish ();
/*
        new Thread () {
            @Override
            public void run () {
                Peer peer = new Peer (device);
                try {
                    peer.connect ();
                    sleep (5000);
                    Log.d (TAG, "send integer");
                    peer.sendCommand (Command.INTEGER_ONE);
                    sleep (10000);
                    Log.d (TAG, "send char");
                    peer.sendCommand (Command.CHAR_ONE);
                    sleep (10000);
                    Log.d (TAG, "send string");
                    peer.sendCommand (Command.STRING_ONE);
                    sleep (10000);
                    peer.sendCommand (Command.QUIT);
                } catch (Exception ex) {
                    try {
                        peer.disconnect ();
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                }
            }
        }.start ();
*/
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction ();
            if (BluetoothDevice.ACTION_FOUND.equals (action)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                String mac = device.getAddress ();
                Log.d (TAG, "receive a device: " + device);
                if (!keys.contains (mac)) {
                    keys.add (mac);
                    adapter.addItem (device);
                    Log.d (TAG, "device " + device + " not in list, add it.");
                } else {
                    Log.d (TAG, "device " + device + " is already in list, ignore it.");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals (action)) {
                Log.d (TAG, "finish discovery");
            }
        }
    }
}