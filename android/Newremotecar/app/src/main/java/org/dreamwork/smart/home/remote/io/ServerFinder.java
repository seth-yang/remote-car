package org.dreamwork.smart.home.remote.io;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;

/**
 */
public class ServerFinder {
    private InetAddress address;
    private int port;

    private final Object locker = new Object ();

    public InetAddress findServer () throws IOException, InterruptedException, ExecutionException {
        List<InetAddress> list = getNetworkInterfaces ();
        if (list == null || list.isEmpty ())
            return null;

        List<FindServerCaller> temp = new ArrayList<FindServerCaller> ();
        ExecutorService executor = Executors.newFixedThreadPool (list.size ());
        int port;

        for (InetAddress address : list) {
            port = (int) (Math.random () * 10000 + 50000);
            FindServerCaller caller = new FindServerCaller (port, address, this);
            temp.add (caller);
            executor.submit (caller);
            broadcast (port, address);
        }
        executor.shutdown ();

        synchronized (locker) {
            locker.wait (60000);
        }

        for (FindServerCaller caller : temp) {
            caller.cancel ();
        }
        temp.clear ();

        return address;
    }

    public void setAddress (InetAddress address, int port) {
        synchronized (locker) {
            if (this.address == null) {
                this.address = address;
                this.port = port;
            }
            locker.notifyAll ();
        }
    }

    public int getRemotePort () {
        return port;
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

    private static void broadcast (int port, InetAddress address) throws IOException {
        byte[] ip = new byte[4];
        ip [0] = (byte) ((port >> 24) & 0xff);
        ip [1] = (byte) ((port >> 16) & 0xff);
        ip [2] = (byte) ((port >> 8) & 0xff);
        ip [3] = (byte) (port & 0xff);

        InetAddress remote = InetAddress.getByName ("255.255.255.255");
        DatagramSocket client = new DatagramSocket ((int) (Math.random () * 1000 + 40000), address);
        DatagramPacket packet = new DatagramPacket (ip, 4, remote, 8899);
        client.send (packet);
        client.close ();
    }

    public static void main (String[] args) throws Exception {
        InetAddress address = new ServerFinder ().findServer ();
        System.out.println (address.getHostAddress ());
        System.out.println ("done.");
    }
}