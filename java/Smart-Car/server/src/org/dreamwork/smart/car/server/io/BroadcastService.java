package org.dreamwork.smart.car.server.io;

import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.util.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 15-2-27
 * Time: 下午2:01
 */
public class BroadcastService implements Runnable {
    private boolean running = true;
    private static final Logger logger = Logger.getLogger (BroadcastService.class);

    private int udpPort;

    public BroadcastService (int udpPort) {
        this.udpPort = udpPort;
    }

    public void bind () throws IOException {
        new Thread (this).start ();
    }

    public synchronized void shutdown () {
        running = false;
    }

    @Override
    public void run () {
        try {
            DatagramSocket server = new DatagramSocket (udpPort);
            ExecutorService executor = Executors.newFixedThreadPool (10);
            logger.info ("--------------------------------------------");
            logger.info (" Server listen on " + server.getLocalAddress () + ":" + udpPort);
            logger.info ("--------------------------------------------");
            while (running) {
                DatagramPacket packet = new DatagramPacket (new byte[4], 4);
                if (logger.isDebugEnabled ())
                    logger.debug ("waiting for message ... ");
                server.receive (packet);
                if (logger.isDebugEnabled ())
                    logger.debug ("get a message !");
                Worker worker = new Worker (packet);
                executor.execute (worker);

                synchronized (this) {
                    if (!running)
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    private static class Worker implements Runnable {
        private DatagramPacket packet;

        public Worker (DatagramPacket packet) {
            this.packet = packet;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         */
        @Override
        public void run () {
            SocketAddress sa = packet.getSocketAddress ();
            InetAddress address = packet.getAddress ();
            byte[] data = packet.getData ();
            int remotePort = ((data [0] & 0xff) << 24) + ((data [1] & 0xff) << 16) +
                    ((data [2] & 0xff) <<  8) +  (data [3] & 0xff);

            Config config = Config.getInstance ();
            int controlPort = config.getIntValue (Config.CAR_REMOTE_PORT, -1);
            int cameraPort = config.getIntValue (Config.CAMERA_PORT, -1);
/*
            byte[] buff = new byte[4];
            buff [0] = (byte) ((port >> 24) & 0xff);
            buff [1] = (byte) ((port >> 16) & 0xff);
            buff [2] = (byte) ((port >>  8) & 0xff);
            buff [3] = (byte) (port & 0xff);
*/

            if (logger.isDebugEnabled ()) {
                logger.debug ("control port : " + controlPort);
                logger.debug ("camera port : " + cameraPort);
                logger.debug ("socket address: " + sa);
                logger.debug ("inet address: " + address.getHostAddress ());
            }
            Socket socket = null;
            try {
                socket = new Socket (address, remotePort);
                OutputStream out = socket.getOutputStream ();
                out.write (int2Bytes (controlPort));
                out.write (int2Bytes (cameraPort));
                out.flush ();
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            } finally {
                if (socket != null) try {
                    socket.close ();
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }
        }
    }

    private static byte[] int2Bytes (int value) {
        byte[] buff = new byte[4];
        buff [0] = (byte) ((value >> 24) & 0xff);
        buff [1] = (byte) ((value >> 16) & 0xff);
        buff [2] = (byte) ((value >>  8) & 0xff);
        buff [3] = (byte) (value & 0xff);
        return buff;
    }
}
