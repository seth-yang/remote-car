package org.dreamwork.smart.car.server.util;

import com.pi4j.io.gpio.*;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class GpioHelper {
    private static final GpioController gpio = GpioFactory.getInstance ();

    public static GpioPinDigitalOutput getDigitalOutputPin (int index) {
        try {
            String name = String.format ("GPIO_%02d", index);
            Pin pin = (Pin) RaspiPin.class.getField (name).get (null);
            return gpio.provisionDigitalOutputPin (pin, name, PinState.LOW);
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }
}