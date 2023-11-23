package org.mqtt;

import org.mqtt.servers.ClientApp;
import org.mqtt.servers.RMIInstance;
import org.mqtt.servers.ServerApp;

import java.util.Locale;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        assert args != null;
        var type = args[0];
        switch (type.toLowerCase(Locale.ROOT)) {
            case "server": {
                ServerApp.main(args);
                break;
            }
            case "client": {
                ClientApp.main(args);
                break;
            }
            default: {
                RMIInstance.main(args);
                break;
            }
        }
    }
}
