package org.dreamwork.smart.car.server.io;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 15-3-3
 * Time: 下午11:03
 */
public class NetworkUtil {
    private static String ip;

    public static InetAddress getFirstNetworkInterface () throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
        InetAddress firstIP = null;
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
                    ip = address.getHostAddress ();
                    System.out.println ("Found IP = " + ip);
                    if (firstIP == null) {
                        firstIP = address;
                        ip = address.getHostAddress ();
                    }
                }
            }
        }
        return firstIP;
    }

    public static List<InetAddress> getNetworkInterfaces () throws SocketException {
        List<InetAddress> ret = new ArrayList<InetAddress> ();
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
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

    public static List<NetworkInterface> getAllNetworkInterfaces () throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
        List<NetworkInterface> list = new ArrayList<> ();
        while (e.hasMoreElements ()) {
            NetworkInterface ni = e.nextElement ();
            if (!ni.isUp () || ni.isLoopback ()) continue;

            byte[] mac = ni.getHardwareAddress ();
            if (mac == null || ni.isPointToPoint ()) continue;

            list.add (ni);
        }
        return list;
    }

    public static String getHostIp () {
        if (ip == null) {
            try {
                getFirstNetworkInterface ();
            } catch (SocketException e) {
                e.printStackTrace ();
            }
        }
        return ip;
    }
}