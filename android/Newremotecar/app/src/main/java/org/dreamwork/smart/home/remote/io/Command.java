package org.dreamwork.smart.home.remote.io;

/**
 */
public enum Command {
    QUIT (-1, false),
    STOP ( 0, false),
    FORWARD (1, false),
    BACKWARD (2, false),
    TURN_LEFT (3, false),
    TURN_RIGHT (4, false),
    TOGGLE_LED (5, false),
    TOGGLE_CAMERA (6, false),
    TOGGLE_LEFT_BLINK (7, false),
    TOGGLE_RIGHT_BLINK (8, false),
    STOP_VERTICAL_SERVO (9, false),
    STOP_HORIZONTAL_SERVO (10, false),
    SPEED (11, false),
    SERVO_UP (12, false),
    SERVO_RIGHT (13, false),
    SERVO_DOWN (14, false),
    SERVO_LEFT (15, false),

    LEFT_FORWARD (101, false),
    LEFT_BACKWARD (102, false),
    LEFT_PAUSE (103, false),
    RIGHT_FORWARD (104, false),
    RIGHT_BACKWARD (105, false),
    RIGHT_PAUSE (106, false)
    ;

    public final int code;
    public final boolean hasReturn;

    private Command (int code, boolean hasReturn) {
        this.code = code;
        this.hasReturn = hasReturn;
    }

    public static Command parse (int code) {
        for (Command cmd : values ()) {
            if (code == cmd.code)
                return cmd;
        }

        return null;
    }
}