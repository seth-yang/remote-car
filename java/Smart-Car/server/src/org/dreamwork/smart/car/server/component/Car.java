package org.dreamwork.smart.car.server.component;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.util.Config;
import org.dreamwork.smart.car.server.util.GpioHelper;
import org.dreamwork.smart.car.server.util.Rotate;

import java.io.*;

/**
 */
public class Car implements ServoListener {
    private static final int BLINK_LEFT = 8, BLINK_RIGHT = 9, LED = 11;
    private static final int SERVO_0 = 4, SERVO_1 = 5;
    private static final int
            DIR_FORWARD = 1, DIR_BACKWARD = -1, DIR_STOP = 0,
            DIR_TURN_LEFT = -2, DIR_TURN_RIGHT = 2;

    private int dir = DIR_STOP;
    private Integer backup_dir = null;
    private Rotate rotate;
    private BlinkLED leftLed, rightLed;
    private Servo servo0, servo1;
    private Motor left_front, /*left_back, */right_front/*, right_back*/;
    private Camera camera;
    private final Config config;
    private GpioPinDigitalOutput led;
    private GpioController gpio;
    private boolean shutdown = false;

    private static final Logger logger = Logger.getLogger (Car.class);

    public Car (Config config) throws IOException, InterruptedException {
        this.config = config;
        setup ();
    }

    private void setup () throws IOException, InterruptedException {
        gpio = GpioFactory.getInstance ();
        // led
        logger.debug ("init all LEDs !");
        leftLed = new BlinkLED (config.getIntValue (Config.BLINK_LEFT, BLINK_LEFT));
        rightLed = new BlinkLED (config.getIntValue (Config.BLINK_RIGHT, BLINK_RIGHT));
        led = GpioHelper.getDigitalOutputPin (config.getIntValue (Config.FRONT_LED, LED));

        // motors
        logger.debug ("init all motors!");
        int[] pins = config.getMotorPins (Config.MOTOR_LEFT_FRONT);
        left_front  = new Motor (pins [0],  pins [1],  new PWM (pins [2], 50));
        left_front.setSpeed (2);

/*
        pins = config.getMotorPins (Config.MOTOR_LEFT_BACK);
        left_back   = new Motor (pins [0],  pins [1],  new PWM (pins [2], 50));
        left_back.setSpeed (2);
*/

        pins = config.getMotorPins (Config.MOTOR_RIGHT_FRONT);
        right_front = new Motor (pins [0],  pins [1],  new PWM (pins [2], 50));
        right_front.setSpeed (2);

/*
        pins = config.getMotorPins (Config.MOTOR_RIGHT_BACK);
        right_back  = new Motor (pins [0],  pins [1],  new PWM (pins [2], 50));
        right_back.setSpeed (2);
*/

        // camera
        camera = new Camera (config.getIntValue (Config.CAMERA_PORT, 8002));

        // servos
        rotate = new Rotate ();

        servo0 = new Servo (config.getIntValue (Config.SERVO_0, SERVO_0));
        servo0.addListener (this);
//        servo0.set (0);
        servo1 = new Servo (config.getIntValue (Config.SERVO_1, SERVO_1), -45, 75);
        servo0.addListener (this);
//        servo1.set (0);

        reset ();

        logger.debug ("init completed.");
//        Thread.sleep (500);
    }

    private void toggleBlinkLed (BlinkLED led) {
        if (led.isBlinking ()) {
            led.pause ();
        } else {
            led.blink ();
        }
    }

    public void reset () throws InterruptedException {
        stop ();
        servo0.reset ();
        servo1.reset ();
        camera.close ();
        logger.debug ("Car reset.");
    }

    public void dispose () throws InterruptedException {
        leftLed.shutdown (true);
        rightLed.shutdown (true);

        servo0.reset ();
        servo1.reset ();

        left_front.dispose ();
//        left_back.dispose ();
        right_front.dispose ();
//        right_back.dispose ();
        gpio.shutdown ();
        shutdown = true;
    }

    public boolean isShutdown () {
        return shutdown;
    }

    public void left_forward () {
        left_front.forward ();
//        left_back.forward ();
    }

    public void left_backward () {
        left_front.backward ();
//        left_back.backward ();
    }

    public void left_pause () {
        left_front.stop ();
//        left_back.stop ();
    }

    public void right_forward () {
        right_front.forward ();
//        right_back.forward ();
    }

    public void right_backward () {
        right_front.backward ();
//        right_back.backward ();
    }

    public void right_pause () {
        right_front.stop ();
//        right_back.stop ();
    }

    public void toggleLeftBlink () {
        toggleBlinkLed (leftLed);
    }

    public void toggleRightBlink () {
        toggleBlinkLed (rightLed);
    }

    public void forward () throws InterruptedException {
        if (dir != DIR_FORWARD) {
            stop ();
            setSpeed (3);
            left_forward ();
            right_forward ();
            dir = DIR_FORWARD;
            leftLed.pause ();
            rightLed.pause ();
        }
    }

    public void backward () throws InterruptedException {
        if (dir != DIR_BACKWARD) {
            stop ();
            setSpeed (3);
            left_backward ();
            right_backward ();
            leftLed.pause ();
            rightLed.pause ();
            dir = DIR_BACKWARD;
        }
    }

    public void stop () throws InterruptedException {
        left_pause ();
        right_pause ();
        dir = DIR_STOP;
        backup_dir = null;
        leftLed.pause ();
        rightLed.pause ();
        Thread.sleep (50);
    }

    public void toggleServoLeft () {
        stopHorizontalRotate ();
        if (rotate.isRotateLeft ()) {
            rotate.x = Rotate.NONE;
        } else {
            servoLeft ();
            rotate.x = Rotate.LEFT;
        }
    }

    public void toggleServoRight () {
        stopHorizontalRotate ();
        if (rotate.isRotateRight ()) {
            rotate.x = Rotate.NONE;
        } else {
            servoRight ();
            rotate.x = Rotate.RIGHT;
        }
    }

    public void toggleServoUp () {
        stopVerticalRotate ();
        if (rotate.isRotateUp ()) {
            rotate.y = Rotate.NONE;
        } else {
            servoUp ();
            rotate.y = Rotate.UP;
        }
    }

    public void toggleServoDown () {
        stopVerticalRotate ();
        if (rotate.isRotateDown ()) {
            rotate.y = Rotate.NONE;
        } else {
            servoDown ();
            rotate.y = Rotate.DOWN;
        }
    }

    public void toggleTurnLeft () throws InterruptedException {
        if (dir != DIR_TURN_LEFT) {
            logger.debug ("first time left");
            turnLeft ();
            if (dir == DIR_FORWARD || dir == DIR_BACKWARD) {
                backup_dir = dir;
            }
            dir = DIR_TURN_LEFT;
        } else {
            stop ();
            if (backup_dir != null) {
                if (backup_dir == DIR_FORWARD) {
                    logger.debug ("forward from turn left");
                    forward ();
                } else if (backup_dir == DIR_BACKWARD) {
                    logger.debug ("backward from turn right");
                    backward ();
                }
            }
        }
    }

    public void toggleTurnRight () throws InterruptedException {
        if (dir != DIR_TURN_RIGHT) {
            turnRight ();
            if (dir == DIR_FORWARD || dir == DIR_BACKWARD) {
                backup_dir = dir;
            }
            dir = DIR_TURN_RIGHT;
        } else {
            stop ();
            if (backup_dir != null) {
                if (backup_dir == DIR_FORWARD) {
                    logger.debug ("forward from turn left");
                    forward ();
                } else if (backup_dir == DIR_BACKWARD) {
                    logger.debug ("backward from turn right");
                    backward ();
                }
            }
/*
            if (backup_dir != null) {
                dir = backup_dir;
                if (backup_dir == DIR_FORWARD) {
                    forward ();
                } else if (backup_dir == DIR_BACKWARD) {
                    backward ();
                }
                backup_dir = null;
            } else {
                stop ();
            }
*/
        }
    }

    private void turnLeft () throws InterruptedException {
        logger.debug ("turn left");
        left_front.setSpeed (5);
        right_front.setSpeed (5);
        if (dir == DIR_BACKWARD) {
            left_forward ();
//            right_backward ();
            right_pause ();
        } else {
//            left_backward ();
            left_pause ();
            right_forward ();
        }
        leftLed.blink ();
    }

    private void turnRight () throws InterruptedException {
        left_front.setSpeed (5);
        right_front.setSpeed (5);
        if (dir == DIR_BACKWARD) {
//            left_backward ();
            left_pause ();
            right_forward ();
        } else {
            left_forward ();
//            right_backward ();
            right_pause ();
        }
        rightLed.blink ();
    }

    public void setSpeed (int speed) {
        this.left_front.setSpeed (speed);
//        this.left_back.setSpeed (speed);
        this.right_front.setSpeed (speed);
//        this.right_back.setSpeed (speed);
    }

    public void toggleLED () {
        led.toggle ();
    }

    public void toggleCamera () throws IOException {
        if (!camera.isOpened ())
            camera.open ();
        else
            camera.close ();
    }

    public void servoLeft () {
        servo0.increase ();
    }

    public void servoRight () {
        servo0.decrease ();
    }

    public void stopHorizontalRotate () {
        servo0.stopRotate ();
    }

    public void servoUp () {
        servo1.decrease ();
    }

    public void servoDown () {
        servo1.increase ();
    }

    public void stopVerticalRotate () {
        servo1.stopRotate ();
    }

    public boolean isRotateUp () {
        return rotate.isRotateUp ();
    }

    public boolean isRotateLeft () {
        return rotate.isRotateLeft ();
    }

    public boolean isRotateDown () {
        return rotate.isRotateDown ();
    }

    public boolean isRotateRight () {
        return rotate.isRotateRight ();
    }

    @Override
    public void onReachBorder (Object sender, int border) {
        if (sender == servo0) {
            rotate.x = Rotate.NONE;
        } else if (sender == servo1) {
            rotate.y = Rotate.NONE;
        }
    }
}