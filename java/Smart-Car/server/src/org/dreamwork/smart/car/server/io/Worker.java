package org.dreamwork.smart.car.server.io;

import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.component.Car;

import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
import java.net.Socket;

public class Worker implements Runnable {
    private final Socket socket;
    private final Car car;

//    private static final byte[] OK = {0, 0, 0, 0};

    public Worker (Socket socket, Car car) {
        this.socket = socket;
        this.car = car;
    }

    private static final Logger logger = Logger.getLogger (Worker.class);

    @Override
    public void run () {
        try {
            InputStream in = socket.getInputStream ();
            logger.debug (socket.getInetAddress ().getHostAddress () + " connect to me.");
//            OutputStream out = socket.getOutputStream ();
            while (true) {
                Command command = readCommand (in);

                switch (command) {
                    case QUIT :
                        return;
                    case TOGGLE_CAMERA :
                        logger.debug ("toggle camera");
                        car.toggleCamera ();
                        break;
                    case TURN_LEFT :
                        logger.debug ("turn left");
                        car.toggleTurnLeft ();
                        break;
                    case TURN_RIGHT :
                        logger.debug ("turn right");
                        car.toggleTurnRight ();
                        break;
                    case FORWARD :
                        logger.debug ("forward");
                        car.forward ();
                        break;
                    case BACKWARD :
                        logger.debug ("backward");
                        car.backward ();
                        break;
                    case STOP :
                        logger.debug ("stop");
                        car.stop ();
                        break;
                    case TOGGLE_LED:
                        logger.debug ("toggle led");
                        car.toggleLED ();
                        break;
                    case TOGGLE_LEFT_BLINK:
                        logger.debug ("toggle left blink");
                        car.toggleLeftBlink ();
                        break;
                    case TOGGLE_RIGHT_BLINK:
                        logger.debug ("toggle right blink");
                        car.toggleRightBlink ();
                        break;
                    case SPEED :
                        int speed = in.read ();
                        logger.debug ("set car speed to " + speed);
                        car.setSpeed (speed);
                        break;
                    case SERVO_UP:
                        logger.debug ("servo up");
                        car.toggleServoUp ();
                        break;
                    case SERVO_RIGHT :
                        logger.debug ("servo right");
                        car.toggleServoRight ();
                        break;
                    case SERVO_DOWN :
                        logger.debug ("servo down");
                        car.toggleServoDown ();
                        break;
                    case SERVO_LEFT :
                        logger.debug ("servo left");
                        car.toggleServoLeft ();
                        break;
                    case LEFT_FORWARD :
                        logger.debug ("left forward");
                        car.left_forward ();
                        break;
                    case LEFT_BACKWARD:
                        logger.debug ("left backward");
                        car.left_backward ();
                        break;
                    case LEFT_PAUSE:
                        logger.debug ("left pause ");
                        car.left_pause ();
                        break;
                    case RIGHT_FORWARD:
                        logger.debug ("right forward");
                        car.right_forward ();
                        break;
                    case RIGHT_BACKWARD:
                        logger.debug ("right backward");
                        car.right_backward ();
                        break;
                    case RIGHT_PAUSE:
                        logger.debug ("right pause");
                        car.right_pause ();
                        break;
                    case STOP_VERTICAL_SERVO:
                        logger.debug ("stop vertical servo");
                        car.stopVerticalRotate ();
                        break;
                    case STOP_HORIZONTAL_SERVO:
                        logger.debug ("stop horizontal servo");
                        car.stopHorizontalRotate ();
                        break;
                    default:
                        System.out.println ("Unknown command");
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        } finally {
            try {
                car.reset ();
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
            try {
                socket.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    private Command readCommand (InputStream in) throws IOException {
        byte[] buff = new byte[4];
        int length = in.read (buff);
        if (length != 4) {
            throw new IOException ("expect 4 bytes, but read " + length + " bytes");
        }
        int code = ((buff [0] & 0xff) << 24) |
                   ((buff [1] & 0xff) << 16) |
                   ((buff [2] & 0xff) <<  8) |
                   (buff [3] & 0xff);
        return Command.parse (code);
    }
}