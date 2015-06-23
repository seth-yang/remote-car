package org.dreamwork.smart.car.server.util;

/**
 * Created by seth.yang on 2015/6/10.
 */
public class Rotate {
    public static final int UP = -1, DOWN = 1;
    public static final int LEFT = -1, RIGHT = 1;
    public static final int NONE = 0;

    public static final int AXIS_X = 0, AXIS_Y = 1;

    public int x = 0, y = 0;

    public boolean isRotateUp () {
        return y == UP;
    }

    public boolean isRotateDown () {
        return y == DOWN;
    }

    public boolean isRotateLeft () {
        return x == LEFT;
    }

    public boolean isRotateRight () {
        return x == RIGHT;
    }
}