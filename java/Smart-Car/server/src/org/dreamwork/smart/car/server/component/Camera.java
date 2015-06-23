package org.dreamwork.smart.car.server.component;

import java.io.IOException;

/**
 * Created by seth.yang on 2015/6/8.
 *
 * //  mjpg_streamer -o "output_http.so -w /usr/www -p 18080" -i "input_raspicam.so -x 1280 -y 720 -fps 15"
 */
public class Camera {
    private Process process;
    private int port;

    public Camera (int port) {
        this.port = port;
    }

    public boolean isOpened () {
        return process != null;
    }

    public void open () throws IOException {
        if (isOpened ())
            return;

        process = new ProcessBuilder (
                "mjpg_streamer",
                "-o", "output_http.so -w /usr/www -p " + port,
                "-i", "input_raspicam.so  -fps 15"
        ).start ();
    }

    public void close () {
        if (process != null) {
            process.destroy ();
            process = null;
        }
    }
}