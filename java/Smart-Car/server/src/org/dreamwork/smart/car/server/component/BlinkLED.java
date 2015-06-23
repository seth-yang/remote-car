package org.dreamwork.smart.car.server.component;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.apache.log4j.Logger;
import org.dreamwork.smart.car.server.util.GpioHelper;
import org.dreamwork.smart.car.server.util.PausableThread;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class BlinkLED extends PausableThread {
    private static final Logger logger = Logger.getLogger (BlinkLED.class);
    private GpioPinDigitalOutput pin;

    public BlinkLED (int pinIndex) {
        super (true);
        pin = GpioHelper.getDigitalOutputPin (pinIndex);
        logger.debug ("blink led standby on " + pinIndex + "!");
    }

    public boolean isBlinking () {
        return !paused;
    }

    public void blink () {
        proceed ();
    }

    @Override
    protected void doWork () {
        try {
            pin.high ();
            sleep (300);
            pin.low ();
            sleep (300);
        } catch (Exception ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }
}