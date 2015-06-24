package org.dreamwork.smart.home.remote.io;

import org.dreamwork.smart.home.remote.Const;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 */
public class FindServerCaller implements Runnable {
    private int port;
    private InetAddress address;
    private ServerSocket server;
    private ServerFinder finder;

    public FindServerCaller (int port, InetAddress address, ServerFinder finder) {
        this.port = port;
        this.address = address;
        this.finder = finder;
    }

    public void run () {
        try {
            server = new ServerSocket (port, -1, address);
            server.setSoTimeout (Const.SOCKET_TIMEOUT);
            server.setReuseAddress (true);
            Socket socket = server.accept ();
            InetAddress address = socket.getInetAddress ();

            InputStream in = socket.getInputStream ();
            byte[] buff = new byte[4];
            int length = in.read (buff, 0, 4);
            if (length != 4) {
                throw new IOException ("Expect 4 bytes, but read " + length + " bytes");
            }

            int port = ((buff[0] & 0xff) << 24) |
                    ((buff[1] & 0xff) << 16) |
                    ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);

            if (Const.DEBUG) {
                System.out.println ("found server ip: " + address + ':' + port);
            }
            finder.setAddress (address, port);
        } catch (SocketException se) {
//
        } catch (Exception e) {
            throw new RuntimeException (e);
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

    public void cancel () throws IOException {
        if (server != null) {
            server.close ();
            System.out.println ("finder canceled.");
        }
    }
}
