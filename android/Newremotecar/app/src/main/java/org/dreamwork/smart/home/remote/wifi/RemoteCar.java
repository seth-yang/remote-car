package org.dreamwork.smart.home.remote.wifi;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by seth.yang on 2015/6/14.
 */
public class RemoteCar implements BroadcastListener {
    private InetAddress ip;
    private int controlPort, cameraPort;

    private static final Object locker = new Object ();
    private static final byte[] MAGIC = {
            (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe,
            'R', 'e', 'm', 'o', 't', 'e', '-', 'c', 'a', 'r' // Remote-car
    };
    private RemoteCar () {}

    public static RemoteCar find (int port) throws IOException, InterruptedException {
        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName ("224.0.0.1");
            DatagramPacket packet = new DatagramPacket (MAGIC, 0, MAGIC.length, group, port);
            socket.send (packet);

            byte[] buff = new byte[12];
            packet = new DatagramPacket (buff, 0, buff.length);
            socket.receive (packet);
            System.out.printf ("received from server: %s%n", packet.getSocketAddress ());
            int control_port = (buff[ 4] & 0xff) << 24 |
                    (buff[ 5] & 0xff) << 16 |
                    (buff[ 6] & 0xff) <<  8 |
                    buff[ 7] & 0xff;
            int camera_port =  (buff[ 8] & 0xff) << 24 |
                    (buff[ 9] & 0xff) << 16 |
                    (buff[10] & 0xff) <<  8 |
                    (buff[11] & 0xff);
            System.out.printf ("control_port = %d, camera_port = %d%n", control_port, camera_port);
            RemoteCar car = new RemoteCar();
            car.ip = packet.getAddress();
            car.cameraPort = camera_port;
            car.controlPort = control_port;
            return car;
        }
/*
        List<InetAddress> list = getNetworkInterfaces ();
        List<BroadcastCaller> futures = new ArrayList<BroadcastCaller> ();
        RemoteCar car = new RemoteCar ();
        int port;
        for (InetAddress address : list) {
            port = (int) (Math.random () * 10000 + 50000);
            BroadcastCaller caller = new BroadcastCaller (port, address, car);
            futures.add (caller);
            caller.start ();
            broadcast (port, udpPort, address);
        }

        synchronized (locker) {
            locker.wait (30000);
        }
        for (BroadcastCaller task : futures) {
            task.abort ();
        }

        if (car.ip != null)
            return car;
        return null;
*/
    }

    public static List<InetAddress> getNetworkInterfaces () throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
        List<InetAddress> ret = new ArrayList<InetAddress> ();
        while (e.hasMoreElements ()) {
            NetworkInterface ni = e.nextElement ();
            if (!ni.isUp ()) continue;
            if (ni.isLoopback ()) continue;
            byte[] mac = ni.getHardwareAddress ();
            if (mac == null) continue;
            if (ni.isPointToPoint ()) continue;
            List<InterfaceAddress> list = ni.getInterfaceAddresses ();
            if (list == null || list.isEmpty ()) continue;

            for (InterfaceAddress ia : list) {
                InetAddress address = ia.getAddress ();
                if (address instanceof Inet4Address) {
                    ret.add (address);
                }
            }
        }
        return ret;
    }

    private static void broadcast (int port, int udpPort, InetAddress address) throws IOException {
        System.out.println ("broadcast to " + address + ":" + udpPort);
        byte[] ip = new byte[4];
        ip [0] = (byte) ((port >> 24) & 0xff);
        ip [1] = (byte) ((port >> 16) & 0xff);
        ip [2] = (byte) ((port >> 8) & 0xff);
        ip [3] = (byte) (port & 0xff);

        InetAddress remote = InetAddress.getByName ("255.255.255.255");
        DatagramSocket client = new DatagramSocket ((int) (Math.random () * 1000 + 40000), address);
        DatagramPacket packet = new DatagramPacket (ip, 4, remote, udpPort);
        client.send (packet);
        client.close ();
    }

    @Override
    public void onReceive (byte[] data) {
        byte[] buff = new byte[4];
        System.arraycopy (data, 0, buff, 0, 4);

        try {
            ip = InetAddress.getByAddress (buff);
            System.out.println ("remote address is: " + ip);
        } catch (UnknownHostException e) {
            e.printStackTrace ();
            throw new RuntimeException (e);
        }

        System.arraycopy (data, 4, buff, 0, 4);
        controlPort = byte2Int (buff);
        System.out.println ("control port: " + controlPort);

        System.arraycopy (data, 8, buff, 0, 4);
        cameraPort = byte2Int (buff);
        System.out.println ("camera port: " + cameraPort);

        synchronized (locker) {
            locker.notifyAll ();
        }
    }

    public InetAddress getIp () {
        return ip;
    }

    public int getControlPort () {
        return controlPort;
    }

    public int getCameraPort () {
        return cameraPort;
    }

    private int byte2Int (byte[] buff) {
        return  ((buff [0] & 0xff) << 24) |
                ((buff [1] & 0xff) << 16) |
                ((buff [2] & 0xff) <<  8) |
                (buff [3] & 0xff);
    }

    @Override
    public String toString () {
        return ip == null ? "" : "remote car {address: " + ip.getHostAddress () + ", control port: " + controlPort + ", camera port: " + cameraPort + "}";
    }
}