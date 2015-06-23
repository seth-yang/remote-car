package org.dreamwork.smart.car.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by seth.yang on 2015/6/14.
 */
public class BroadcastCaller extends Thread {
    private int port;
    private InetAddress address;
    private BroadcastListener listener;
    private ServerSocket server;

    private static final int SOCKET_TIMEOUT = 10000;

    public BroadcastCaller (int port, InetAddress address, BroadcastListener listener) {
        this.port = port;
        this.address = address;
        this.listener = listener;
    }

    @Override
    public void run () {
        try {
            server = new ServerSocket (port, -1, address);
            server.setSoTimeout (SOCKET_TIMEOUT);
            server.setReuseAddress (true);
            Socket socket = server.accept ();
            System.out.println ("inet address = " + socket.getInetAddress ());
            System.out.println ("remote address = " + socket.getRemoteSocketAddress ());
            InputStream in = socket.getInputStream ();

            byte[] buff = new byte[8];
            int length = in.read (buff, 0, 8);
            if (length != 8) {
                throw new IOException ("Expect 8 bytes, but read " + length + " bytes");
            }

            byte[] data = new byte[12];
            InetAddress remoteAddress = socket.getInetAddress ();
            System.arraycopy (remoteAddress.getAddress (), 0, data, 0, 4);
            System.arraycopy (buff, 0, data, 4, 8);

            if (listener != null) {
                listener.onReceive (data);
            }
        } catch (Exception e) {
/*
            e.printStackTrace ();
            throw new RuntimeException (e);
*/
        } finally {
            if (server != null && !server.isClosed ()) {
                try {
                    server.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
    }

    public void abort () throws IOException {
        if (server != null) {
            server.close ();
        }
    }
}
