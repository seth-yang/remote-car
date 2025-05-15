package org.dreamwork.smart.car.server.component;

import com.pi4j.gpio.extension.pca.PCA9685Pin;
import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.util.PausableThread;
import org.dreamwork.smart.car.server.util.TimeoutListener;
import org.dreamwork.smart.car.server.util.TimeoutMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 PCA9685 扩展板的 舵机对象.
 * Created by seth.yang on 2015/6/8.
 */
public class Servo extends PWM implements TimeoutListener {
    public enum Direction {
        INCREMENT, DECREMENT
    }

    public static final int SERVO_DURATION_MIN = 500;
    public static final int SERVO_DURATION_NEUTRAL = 1500;
    public static final int SERVO_DURATION_MAX = 2500;
    private static final int INTERVAL_BASE = 240;

    private static final Logger logger = Logger.getLogger (Servo.class);

    private int angle = 0;
    private int step = 2;
    private int minAngle = -90, maxAngle = 90;
    private long interval = 80;

    private Direction dir = Direction.INCREMENT;

    private final PausableThread worker;
    private final TimeoutMonitor monitor;

    private final List<ServoListener> list = new ArrayList<> ();

    public Servo (int pin) throws IOException {
        this (pin, -90, 90);
    }

    public Servo (int pin, final int min, final int max) throws IOException {
        super (pin, 50);
        this.minAngle = min;
        this.maxAngle = max;
        worker = new PausableThread (true) {
            @Override
            protected void doWork () {
                if ((dir == Direction.INCREMENT && angle >= maxAngle) ||
                    (dir == Direction.DECREMENT && angle <= minAngle)) {
                    logger.debug ("reach border [" + minAngle + ", " + maxAngle + "], pause the thread.");
                    fireListener (angle);
                    pause ();
                } else {
                    try {
                        int delta = dir == Direction.INCREMENT ? step : -step;
                        offset (delta);
                        sleep (interval);
                    } catch (InterruptedException ex) {
                        logger.warn (ex.getMessage (), ex);
                    }
                }
            }
        };
        monitor = new TimeoutMonitor (2000);
        monitor.addTimeoutListener (this);
        monitor.start ();
    }

    public void set (int angle) throws InterruptedException {
        if (angle < minAngle)
            angle = minAngle;
        if (angle > maxAngle)
            angle = maxAngle;
        this.angle = angle;
        double tmp = angle + 90;
        double duration = SERVO_DURATION_MIN + 100 * tmp / 9;
        if (duration > SERVO_DURATION_MAX) duration = SERVO_DURATION_MAX;
        else if (duration < SERVO_DURATION_MIN) duration = SERVO_DURATION_MIN;
        provider.setPwm (PCA9685Pin.ALL [pinIndex], (int) duration);
        monitor.touch ();
        logger.debug ("set servo #" + pin + " to angle: " + angle + ", pwm = " + duration);
    }

    public int get () {
        return angle;
    }

    public void reset () {
/*
        new Thread (new Runnable () {
            @Override
            public void run () {
*/
                try {
                    set (0);
                    Thread.sleep (300);
                    logger.debug ("Servo #" + pin + " reset.");
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
/*
            }
        });
*/
    }

    public void setStep (int step) {
        this.step = step;
    }

    public void increase () {
        dir = Direction.INCREMENT;
        worker.proceed ();
    }

    public void decrease () {
        dir = Direction.DECREMENT;
        worker.proceed ();
    }

    public void stopRotate () {
        worker.pause ();
    }

    public void offset (int deltaAngle) throws InterruptedException {
        set (angle + deltaAngle);
    }

    public void setSpeed (int speed) {
        if (speed < 1) speed = 1;
        if (speed > 5) speed = 5;

        interval = INTERVAL_BASE / speed;
    }

    public void dispose () throws InterruptedException {
        worker.shutdown (true);
        reset ();
    }

    public void addListener (ServoListener listener) {
        list.add (listener);
    }

    protected void fireListener (int border) {
        for (ServoListener listener : list) {
            listener.onReachBorder (this, border);
        }
    }

    @Override
    public void onTimeout () {
        provider.setAlwaysOff (PCA9685Pin.ALL [pinIndex]);
    }
}