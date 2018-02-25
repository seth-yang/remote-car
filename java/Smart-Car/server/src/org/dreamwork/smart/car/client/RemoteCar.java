package org.dreamwork.smart.car.client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by seth.yang on 2015/6/14
 */
public class RemoteCar {
    private InetAddress ip;
    private int controlPort, cameraPort;

    private static final byte[] MAGIC = {
            (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe,
            'R', 'e', 'm', 'o', 't', 'e', '-', 'c', 'a', 'r' // Remote-car
    };

    private RemoteCar () {}

    public InetAddress getIp () {
        return ip;
    }

    public int getControlPort () {
        return controlPort;
    }

    public int getCameraPort () {
        return cameraPort;
    }

    public static RemoteCar find (int port) throws IOException {
        InetAddress target = InetAddress.getByName ("255.255.255.255");
        byte[] buff = new byte[12];  // magic(4) + tcp_port(4) + camera_port(4)
        DatagramPacket received = new DatagramPacket (buff, buff.length);
        DatagramPacket packet   = new DatagramPacket (MAGIC, MAGIC.length, target, port);
        DatagramSocket socket   = new DatagramSocket ();
        socket.setSoTimeout (60000); // time-out in 60 seconds
        socket.send (packet);
        socket.receive (received);   // waiting for the remote-car response
        // there, we got peer's response
        // or catch the IOException that thrown
        int magic  = readInt (buff, 0);
        int tcp    = readInt (buff, 4);
        int camera = readInt (buff, 8);

        System.out.println (received.getSocketAddress ());
        SocketAddress sa = received.getSocketAddress ();
        System.out.println (sa.getClass ());

        RemoteCar car   = new RemoteCar ();
        car.cameraPort  = camera;
        car.controlPort = tcp;
        car.ip          = received.getAddress ();
        return car;
    }

    @Override
    public String toString () {
        return ip == null ? "" : "remote car {address: " + ip.getHostAddress () + ", control port: " + controlPort + ", camera port: " + cameraPort + "}";
    }

    public static void main (String[] args) throws Exception {
        RemoteCar car = find (8001);
        if (car != null) {
            System.out.println (car);
        } else {
            System.out.println ("Can't find remote car");
        }
    }

    private static int readInt (byte[] data, int offset) {
        return  ((data [offset ++] & 0xff) << 24) |
                ((data [offset ++] & 0xff) << 16) |
                ((data [offset ++] & 0xff) <<  8) |
                ((data [offset   ] & 0xff));
    }
}