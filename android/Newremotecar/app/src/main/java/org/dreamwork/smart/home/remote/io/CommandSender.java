package org.dreamwork.smart.home.remote.io;

import org.dreamwork.smart.home.remote.data.Command;
import org.dreamwork.smart.home.remote.data.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 */
public class CommandSender extends Thread {
    private final Socket socket;
    private InputStream in;
    private OutputStream out;
    private final BlockingQueue<Command> queue = new ArrayBlockingQueue<> (16);
    private final List<CommandListener> listeners = new ArrayList<> ();

    public CommandSender (InetAddress address, int port) throws IOException {
        socket = new Socket (address, port);
        connect ();
    }

    public CommandSender (String address, int port) throws IOException {
        socket = new Socket (address, port);
        connect ();
    }

    public void disconnect () {
        if (socket != null && !socket.isConnected ()) {
            try {
                socket.close ();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public boolean sendCommand (Command command) throws InterruptedException {
        return queue.offer (command, 10, TimeUnit.SECONDS);
    }

    public void addListener (CommandListener listener) {
        if (!listeners.contains (listener)) {
            listeners.add (listener);
        }
    }

    private void connect () throws IOException {
        in = socket.getInputStream ();
        out = socket.getOutputStream ();
    }

    private void fireListener (Response<?> response) {
        List<CommandListener> copy = new ArrayList<> (listeners.size ());
        copy.addAll (listeners);
        for (CommandListener listener : copy) {
            listener.onResponse (response);
        }
    }

    @Override
    public void run () {
        while (true) {
            try {
                Command command = queue.take ();
                if (command == Command.QUIT) {
                    break;
                } else {
                    byte[] buff = new byte[4];
                    buff[0] = (byte) ((command.code >> 24) & 0xff);
                    buff[1] = (byte) ((command.code >> 16) & 0xff);
                    buff[2] = (byte) ((command.code >>  8) & 0xff);
                    buff[3] = (byte) (command.code & 0xff);
                    out.write (buff);
                    out.flush ();

                    if (command.hasReturn) {
                        Response response = readResponse (in);
                        fireListener (response);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
        }
    }

    private Response readResponse (InputStream in) throws IOException {
        int status = in.read ();
        if (status == -1) {
            throw new RuntimeException ("IO Exception");
        }
        int type = in.read ();
        if (type == -1) {
            throw new RuntimeException ("IO Exception");
        }
        byte[] buff = new byte[2];
        int length = in.read (buff);
        if (length != 2) {
            throw new IOException ("expect 2 bytes, but read " + length + " bytes.");
        }
        int packageLength = (buff [0] & 0xff) + ((buff [1] & 0xff) << 8);
        buff = new byte[packageLength];
        length = in.read (buff);
        if (length != packageLength) {
            throw new IOException ("expect " + packageLength + " bytes, but read " + length + " bytes.");
        }

        return Response.parseResponse (status, type, buff);
    }
}