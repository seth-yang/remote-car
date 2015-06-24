package org.dreamwork.smart.home.remote.data;

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
    TOGGLE_CAMERA (6, true);

    public final int code;
    public final boolean hasReturn;

    private Command (int code, boolean hasReturn) {
        this.code = code;
        this.hasReturn = hasReturn;
    }
/*
    public static final Command
        QUIT          = new Command (-1, false),
        STOP          = new Command (0, false),
        FORWARD       = new Command (1, false),
        BACKWARD      = new Command (2, false),
        TURN_LEFT     = new Command (3, false),
        TURN_RIGHT    = new Command (4, false),
        TOGGLE_LED    = new Command (5, false),
        TOGGLE_CAMERA = new Command (6, true);

    private static final Command[] COMMANDS = {
        QUIT, STOP, FORWARD, BACKWARD, TURN_LEFT, TURN_RIGHT, TOGGLE_CAMERA, TOGGLE_LED
    };
*/
    public static Command parse (int code) {
        for (Command cmd : values ()) {
            if (code == cmd.code)
                return cmd;
        }

        return null;
    }
}