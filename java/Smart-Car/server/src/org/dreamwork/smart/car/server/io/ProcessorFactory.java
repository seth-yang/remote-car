package org.dreamwork.smart.car.server.io;

import org.dreamwork.smart.car.server.component.Car;
import org.dreamwork.smart.car.server.util.Config;

import java.io.IOException;
import java.net.Socket;

/**
 */
public class ProcessorFactory {
    private Car car;

    public synchronized Runnable getProcessor (Socket socket) throws IOException, InterruptedException {
        if (car == null || car.isShutdown ()) {
            Config config = Config.getInstance ();
            car = new Car (config);
        }
        return new Worker (socket, car);
    }
}