package org.dreamwork.smart.car.server.component;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.dreamwork.smart.car.server.util.GpioHelper;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class Motor {
    private GpioPinDigitalOutput pin0, pin1;
    private PWM pwm;
    private int speed = 3;

    public static final int MAX_SPEED = 5, MIN_SPEED = 0;

    public Motor (int pin0, int pin1, PWM pwm) {
        this (
                GpioHelper.getDigitalOutputPin (pin0),
                GpioHelper.getDigitalOutputPin (pin1),
                pwm
        );
    }

    public Motor (GpioPinDigitalOutput pin0, GpioPinDigitalOutput pin1, PWM pwm) {
        this.pin0 = pin0;
        this.pin1 = pin1;
        this.pwm = pwm;
    }

    public int getSpeed () {
        return speed;
    }

    public void setSpeed (int speed) {
        if (speed < MIN_SPEED) speed = MIN_SPEED;
        if (speed > MAX_SPEED) speed = MAX_SPEED;
        this.speed = speed;
        pwm.setValue (.2f * speed);
    }

    public void forward () {
        pin0.high ();
        pin1.low ();
    }

    public void backward () {
        pin0.low ();
        pin1.high ();
    }

    public void stop () {
        pin0.low ();
        pin1.low ();
    }

    public void dispose () {
        pin0.low ();
        pin1.low ();
        pwm.setValue (0);
    }
}