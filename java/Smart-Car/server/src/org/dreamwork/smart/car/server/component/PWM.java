package org.dreamwork.smart.car.server.component;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class PWM {
    protected PCA9685GpioProvider provider;

    protected static Map<Integer, Pin> PWMS = new ConcurrentHashMap<Integer, Pin> (16);
    protected static GpioController gpio = GpioFactory.getInstance ();

    private static final Object locker = new Object ();

    private static I2CBus bus;

    private static void init () {
        try {
            synchronized (locker) {
                bus = I2CFactory.getInstance (I2CBus.BUS_1);
            }
        } catch (IOException ex) {
            throw new RuntimeException (ex);
        }
    }

    protected int pinIndex;
    protected Pin pin;
    protected float value;

    public PWM (int pin, int frequency) throws IOException {
        this (pin, new BigDecimal (frequency));
    }

    public PWM (int pin, BigDecimal frequency) throws IOException {
        if (provider == null) {
            init ();
        }

        if (PWMS.containsKey (pin))
            throw new IllegalStateException ("Pin #" + pin + " is in use.");

        this.pinIndex = pin;
        this.pin = PCA9685Pin.ALL [pin];

        provider = new PCA9685GpioProvider (bus, 0x40, frequency);
        gpio.provisionPwmOutputPin(provider, PCA9685Pin.ALL [pin], "Pulse 00");
        PWMS.put (pin, this.pin);
    }

    public void setValue (float value) {
        if (value <= 0) {
            this.value = 0;
            provider.setAlwaysOff (pin);
        } else if (value >= 1) {
            this.value = 1;
            provider.setAlwaysOn (pin);
        } else {
            this.value = value;
            provider.setPwm (pin, 0, (int) (this.value * 4095));
        }
    }

    public float getValue () {
        return value;
    }
}
