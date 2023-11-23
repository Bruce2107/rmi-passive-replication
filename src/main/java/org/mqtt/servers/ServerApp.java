package org.mqtt.servers;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mqtt.echo.Echo;
import org.mqtt.echo.EchoServer;
import org.mqtt.services.MqttService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerApp {
    static String host = "//localhost";
    static String port = "8088";
    static String serviceName = "Echo";
    static private MqttService mqttService;

    private static EchoServer echoServer;

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            String serverName = getServerName(0);
            mqttService = new MqttService(serverName);
            echoServer = new EchoServer(mqttService);
            bindName(echoServer, serverName);
            if (notMaster(serverName)) {
                mqttService.subscribe();
            }
            mqttService.setMqttCallBack(callback(echoServer));
            if (notMaster(serverName)) {
                getHistory();
                healthCheckMaster();
            }
            System.out.println("Servidor ativo: " + serverName);
        } catch (Exception e) {
            System.err.println("Exceção no servidor Echo: " + e.getMessage());
        }
    }

    private static String getServerName(int i) throws MalformedURLException, RemoteException {
        String serverName = i == 0 ? "master" : String.format("Clone/%d", i);
        String fullAddress = getFullAddress(serverName);
        try {
            Naming.lookup(fullAddress);
        } catch (NotBoundException e) {
            return serverName;
        }
        return getServerName(++i);
    }

    private static boolean notMaster(String serverName) {
        return !serverName.contains("master");
    }

    private static Echo getMaster() throws MalformedURLException, NotBoundException, RemoteException {
        return (Echo) Naming.lookup("//localhost:8088/Echo/master");
    }

    private static Echo getClone(String cloneName) throws MalformedURLException, NotBoundException, RemoteException {
        return (Echo) Naming.lookup("//localhost:8088/"+cloneName);
    }

    private static void healthCheckMaster() {
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                var master = getMaster();
                while (true) {
                    master.healthCheck();
                    Thread.sleep(1000);
                    System.out.println("Server Master still alive!");
                }
            } catch (RemoteException e) {
                electNewMaster();
            } catch (MalformedURLException | NotBoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 5, TimeUnit.SECONDS);

    }

    private static void getHistory() {
        try {
            var master = getMaster();
            var history = master.getListOfMsg();
            echoServer.updateHistory(history);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getOrder() {
        var name = mqttService.name;
        return Integer.parseInt(name.split("/")[1]);
    }

    private static void electNewMaster() {
        try {
            var registry = LocateRegistry.getRegistry(8088);
            var list = Arrays.stream(registry.list()).filter(n -> !n.contains("master")).collect(Collectors.toList());
            var order = 0;
            var cloneName = "";
            for (String i : list) {
                var n = Integer.parseInt(Arrays.stream(i.split("/")).toArray()[2].toString());
                if (n < order || order == 0) {
                    order = n;
                    cloneName = i;
                }
            }
            var myOrder = getOrder();
            if (myOrder == order) {
                registry.unbind(cloneName);
                mqttService.unsubscribe();
                bindName(echoServer,"master");
            } else {
                try {
                    var newMasterClone = getClone(cloneName);
                    newMasterClone.healthCheck();
                } catch (RemoteException e) {
                    registry.unbind(cloneName);
                    electNewMaster();
                }
            }
        } catch (RemoteException | NotBoundException | MqttException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void bindName(EchoServer echoServer, String serverName) throws MalformedURLException, RemoteException {
        String fullAddress = getFullAddress(serverName);
        Naming.rebind(fullAddress, echoServer);
    }

    private static String getFullAddress(String serverName) {
        return String.format("%s:%s/%s/%s", host, port, serviceName, serverName);
    }

    private static MqttCallback callback(EchoServer echoServer) {
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Disconected from Mqtt Server!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                echoServer.addMessage(mqttMessage.toString());
                System.out.println(echoServer.getListOfMsg());
                System.out.println("Mensagem adicionada no servidor: " + mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
    }
}
