package org.dreamwork.smart.car.server.io;

import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.util.Config;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 15-2-27
 * Time: 下午2:01
 */
public class BroadcastService implements Runnable {
    private boolean running = true;
    private DatagramSocket server;
    private final ExecutorService executor;

    private static final Logger logger = Logger.getLogger (BroadcastService.class);

    private static final int MAX = 32;
    private static final int RESPONSE_LENGTH = 12;           // 4 bytes header + 4 bytes tcp port + 4 bytes camera port
    private static final byte[] MAGIC = {
            (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe,
            'R', 'e', 'm', 'o', 't', 'e', '-', 'c', 'a', 'r' // Remote-car
    };

    private final int port;

    public BroadcastService (int port, ExecutorService executor) {
        this.port     = port;
        this.executor = executor;
    }

    public void bind () throws IOException {
        executor.execute (this);
    }

    public synchronized void shutdown () {
        if (server != null) {
            server.close ();
        }
        server  = null;
        running = false;
    }

    @Override
    public void run () {
        Thread.currentThread ().setName ("BroadcastService");
        try (MulticastSocket socket = new MulticastSocket ()) {
            socket.setTimeToLive (32);
            SocketAddress addr = new InetSocketAddress ("224.0.0.1", port);
            NetworkUtil.getAllNetworkInterfaces ().forEach (ni -> {
                try {
                    socket.joinGroup (addr, ni);
                } catch (IOException e) {
                    throw new RuntimeException (e);
                }
            });

            while (running) {
                if (logger.isDebugEnabled ())
                    logger.debug ("waiting for message ... ");
                DatagramPacket packet = new DatagramPacket (new byte[MAX], MAX);
                socket.receive (packet);
                if (logger.isDebugEnabled ())
                    logger.debug ("get a message !");

                executor.execute (new Worker (packet));
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    private static class Worker implements Runnable {
        private DatagramPacket packet;

        Worker (DatagramPacket packet) {
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
            int offset  = packet.getOffset ();
            int length  = packet.getLength ();
            {
                if (length != data.length) {
                    byte[] tmp = new byte[length];
                    System.arraycopy (data, offset, tmp, 0, length);
                    data = tmp;
                }
            }

            if (Arrays.equals (data, MAGIC)) {
                if (logger.isDebugEnabled ()) {
                    logger.debug ("we got a valid request");
                    logger.debug ("sending the control port and camera port to peer.");
                }

                DatagramSocket socket = null;
                try {
                    Config config = Config.getInstance ();
                    int controlPort = config.getIntValue (Config.CAR_REMOTE_PORT, -1);
                    int cameraPort = config.getIntValue (Config.CAMERA_PORT, -1);
                    if (logger.isDebugEnabled ()) {
                        logger.debug ("control port : " + controlPort);
                        logger.debug ("camera port : " + cameraPort);
                        logger.debug ("socket address: " + sa);
                        logger.debug ("inet address: " + address.getHostAddress ());
                    }

                    socket = new DatagramSocket ();
                    byte[] buff = new byte[RESPONSE_LENGTH];
                    System.arraycopy (int2Bytes (0xcafebabe),  0, buff, 0, 4);
                    System.arraycopy (int2Bytes (controlPort), 0, buff, 4, 4);
                    System.arraycopy (int2Bytes (cameraPort),  0, buff, 8, 4);
                    packet = new DatagramPacket (buff, 0, RESPONSE_LENGTH, sa);
                    socket.send (packet);
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                } finally {
                    if (socket != null) {
                        socket.close ();
                    }
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
