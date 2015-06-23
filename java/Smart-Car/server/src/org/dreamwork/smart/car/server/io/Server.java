package org.dreamwork.smart.car.server.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private ServerSocket server;
    private String name;
    private int port;
    private ProcessorFactory factory;

    public Server (String name, int port, ProcessorFactory factory) {
        this.name = name;
        this.port = port;
        this.factory = factory;
    }

    public void bind () throws IOException {
        server = new ServerSocket (port);
        System.out.println ("Server [" + name + "] listen on: " + server.getLocalSocketAddress ());
        new Thread (this).start ();
    }

    public void unbind () throws IOException {
        if (!server.isClosed ()) {
            server.close ();
        }
    }

    @Override
    public void run () {
        while (!server.isClosed ()) {
            try {
                Socket socket = server.accept ();
                Runnable runner = factory.getProcessor (socket);
                new Thread (runner).start ();
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
        }
    }
}