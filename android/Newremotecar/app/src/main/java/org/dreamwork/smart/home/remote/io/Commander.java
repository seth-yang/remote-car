package org.dreamwork.smart.home.remote.io;

import android.util.Log;
import org.dreamwork.smart.home.remote.Const;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Commander extends Thread {
    private final BlockingQueue<Command> queue;
    private Socket socket = null;
    private final String ip;
    private final int port;
    private final ConnectionListener listener;

    public Commander (String ip, int port, ConnectionListener listener) throws IOException {
        this.queue = new ArrayBlockingQueue<Command> (64);
        this.ip = ip;
        this.port = port;
        this.listener = listener;
    }

    public void sendCommand (Command command) throws InterruptedException {
        queue.offer (command, 3, TimeUnit.SECONDS);
    }

    @Override
    public void run () {
        try {
            socket = new Socket (ip, port);
            Log.d (Const.TAG, "Connect to remote car: " + ip + ':' + port);
            listener.onConnect ();
            OutputStream out = socket.getOutputStream ();
            while (true) {
                try {
                    Command cmd = queue.take ();
                    sendCommand (cmd, out);
                    if (cmd == Command.QUIT) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace ();
        } finally {
            if (socket != null) {
                try {
                    socket.close ();
                } catch (IOException ex) {
                    ex.printStackTrace ();
                }
            }
        }
    }

    private void sendCommand (Command command, OutputStream out) throws IOException {
        byte[] buff = new byte[4];
        buff [0] = (byte) ((command.code >> 24) & 0xff);
        buff [1] = (byte) ((command.code >> 16) & 0xff);
        buff [2] = (byte) ((command.code >>  8) & 0xff);
        buff [3] = (byte) ((command.code) & 0xff);
        out.write (buff);
        out.flush ();
    }
}