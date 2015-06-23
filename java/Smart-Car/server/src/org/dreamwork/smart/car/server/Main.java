package org.dreamwork.smart.car.server;

import org.dreamwork.smart.car.server.io.BroadcastService;
import org.dreamwork.smart.car.server.io.ProcessorFactory;
import org.dreamwork.smart.car.server.io.Server;
import org.dreamwork.smart.car.server.util.Config;

import java.io.File;
import java.net.URL;

/**
 * Created by seth.yang on 2015/6/8.
 */
public class Main {
    public static void main (String[] args) throws Exception {
        String conf = "../conf/car.cfg";
        URL url = null;
        File file;
        if (args.length > 0) {
            String path = args [0];
            System.out.println ("param [0] = " + path);
            file = new File (path);
            if (file.exists ()) {
                System.out.println ("using config file: " + file.getCanonicalPath ());
                url = file.toURI ().toURL ();
            }
        } else {
            file = new File (conf);
            if (file.exists ()) {
                System.out.println ("using config file: " + file.getCanonicalPath ());
                url = file.toURI ().toURL ();
            }
        }

        if (url != null) {
            System.out.println ("trying to load config from " + url.toString ());
            Config.loadConfig (url);
        } else {
            System.out.println ("WARNING Can't find config file!!!");
        }

        Config config = Config.getInstance ();
        int udpPort = config.getIntValue (Config.CAR_UDP_PORT, 8001);
        int tcpPort = config.getIntValue (Config.CAR_REMOTE_PORT, 8000);
        ProcessorFactory factory = new ProcessorFactory ();
        Server server = new Server ("Smart Car Remote Control Server", tcpPort, factory);
        server.bind ();

        BroadcastService service = new BroadcastService (udpPort);
        service.bind ();

        System.out.println ("All Service Started.");
    }
}