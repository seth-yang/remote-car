package test.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
* Created by seth.yang on 2015/6/23.
*/
class Peer extends Thread {
    private boolean running = true;
    private BluetoothDevice peer;
    private BlockingQueue<Command> queue = new ArrayBlockingQueue<Command> (16);

    private BluetoothSocket socket;
    private OutputStream out;

    public Peer (BluetoothDevice peer) {
        this.peer = peer;
    }

    @Override
    public void run () {
        while (running) {
            try {
                Command command = queue.take ();
                if (command != null) {
/*
                    switch (command) {
                        case INTEGER_ONE :
                        case INTEGER_TWO :
                            write ((Integer) command.value);
                            break;
                        case CHAR_ONE :
                            write ((Character) command.value);
                            break;
                        case STRING_ONE :
                            write ((String) command.value);
                            break;
                        case QUIT :
                            disconnect ();
                            return;
                    }
*/
                    if (command == Command.QUIT) {
                        disconnect ();
                        return;
                    } else {
                        write (command.code);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    public void connect () throws IOException {
        Log.d (BT.TAG, "trying to connect to peer: " + peer);
        socket = peer.createRfcommSocketToServiceRecord (BT.SPP_UUID);
        socket.connect ();
        if (socket.isConnected ()) {
            Log.d (BT.TAG, "Bluetooth connected.");
            out = socket.getOutputStream ();
            start ();
        } else {
            Log.d (BT.TAG, "Can't connect to peer.");
        }
    }

    public void sendCommand (Command command) {
        if (out == null) {
            Log.e (BT.TAG, "Peer not connected.");
        } else {
            try {
                queue.offer (command, 3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    public void disconnect () throws IOException {
        out.flush ();
        out.close ();
        socket.close ();
    }

    private void write (int value) throws IOException {
        out.write (value);
        out.flush ();
    }

    private void write (char value) throws IOException {
        write ((int) value);
    }

    private void write (String value) throws IOException {
        byte[] buff = value.getBytes ("utf-8");
        out.write (buff);
        out.flush ();
    }
}
