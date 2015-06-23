package org.dreamwork.smart.car.server.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class Config {
    private static Properties props = new Properties ();

    public static final String MOTOR_LEFT_0_0 = "motor.left.0.0";
    public static final String MOTOR_LEFT_0_1 = "motor.left.0.1";
    public static final String MOTOR_LEFT_1_0 = "motor.left.1.0";
    public static final String MOTOR_LEFT_1_1 = "motor.left.1.1";
    public static final String MOTOR_RIGHT_0_0 = "motor.right.0.0";
    public static final String MOTOR_RIGHT_0_1 = "motor.right.0.1";
    public static final String MOTOR_RIGHT_1_0 = "motor.right.1.0";
    public static final String MOTOR_RIGHT_1_1 = "motor.right.1.1";
    public static final String BLINK_LEFT = "blink.left";
    public static final String BLINK_RIGHT = "blink.right";
    public static final String FRONT_LED = "front.led";
    public static final String MOTOR_LEFT_0_PWM = "motor.left.0.pwm";
    public static final String MOTOR_LEFT_1_PWM = "motor.left.1.pwm";
    public static final String MOTOR_RIGHT_0_PWM = "motor.right.0.pwm";
    public static final String MOTOR_RIGHT_1_PWM = "motor.right.1.pwm";
    public static final String SERVO_0 = "servo.0";
    public static final String SERVO_1 = "servo.1";
    public static final String CAR_REMOTE_PORT = "car.remote.port";
    public static final String CAR_UDP_PORT = "car.udp.port";
    public static final String CAMERA_PORT = "camera.port";

    public static final int
            MOTOR_LEFT_FRONT = 0, MOTOR_LEFT_BACK = 1,
            MOTOR_RIGHT_FRONT = 2, MOTOR_RIGHT_BACK = 3;
    private static final String[][] MOTOR_NAMES = {
            {MOTOR_LEFT_0_0,  MOTOR_LEFT_0_1,  MOTOR_LEFT_0_PWM},
            {MOTOR_LEFT_1_0,  MOTOR_LEFT_1_1,  MOTOR_LEFT_1_PWM},
            {MOTOR_RIGHT_0_0, MOTOR_RIGHT_0_1, MOTOR_RIGHT_0_PWM},
            {MOTOR_RIGHT_1_0, MOTOR_RIGHT_1_1, MOTOR_RIGHT_1_PWM}
    };

    private static final Logger logger = Logger.getLogger (Config.class);
    private static Config instance = new Config ();

    private Config () {
    }

    public static Config getInstance () {
        return instance;
    }

    public static void loadConfig (URL url) {
        try {
            InputStream in = url.openStream ();
            props.load (in);
        } catch (IOException ex) {
            logger.fatal (ex.getMessage (), ex);
            throw new RuntimeException (ex);
        }
    }

    public String getStringValue (String name) {
        String s = props.getProperty (name);
        if (logger.isDebugEnabled ()) {
            logger.debug (name + " = " + s);
        }
        return s;
    }

    public void setValue (String name, String value) {
        props.setProperty (name, value);
    }

    public int getIntValue (String name, int defaultValue) {
        String text = getStringValue (name);
        if (text != null) {
            try {
                return Integer.parseInt (text);
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            }
        }
        return defaultValue;
    }

    public int[] getMotorPins (int motorIndex) {
        if (motorIndex < MOTOR_LEFT_FRONT || motorIndex > MOTOR_RIGHT_BACK)
            throw new ArrayIndexOutOfBoundsException (motorIndex);

        String[] names = MOTOR_NAMES [motorIndex];
        int[] pins = new int[names.length];
        for (int i = 0; i < names.length; i ++) {
            pins [i] = getIntValue (names [i], -1);
        }
        return pins;
    }

    public void save (Writer writer) throws IOException {
        props.store (writer, "Smart Car Config saved at: " + new Date ());
    }
}