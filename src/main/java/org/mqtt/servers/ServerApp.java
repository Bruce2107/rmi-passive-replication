package org.mqtt.servers;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mqtt.echo.Echo;
import org.mqtt.echo.EchoServer;
import org.mqtt.services.MqttService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerApp {
    static String host = "//localhost";
    static String port = "8088";
    static String serviceName = "EchoServer";
    static private MqttService mqttService;

    private static EchoServer echoServer;

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            initiateRegisty();
            String serverName = getServerName(0);
            mqttService = new MqttService(serverName);
            echoServer = new EchoServer(mqttService);
            bindName(echoServer, serverName);
            if (serverName.contains("Clone")) {
                mqttService.subscribe();
            }
            mqttService.setMqttCallBack(callback(echoServer));
            if (notMaster(serverName)) {
                getHistory();
                healthCheckMaster();
            }
            System.out.println("ObjetoServidor esta ativo! Com nome de servidor: " + serverName);
        } catch (Exception e) {
            System.err.println("Exceção no servidor Echo: " + e.getMessage());
            e.printStackTrace();
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

    private static void initiateRegisty() {
        try {
            LocateRegistry.createRegistry(8088);
        } catch (ExportException e) {
            System.out.println("Registry já iniciado!");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    private static void healthCheckMaster() {
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                Echo objetoRemoto = (Echo) Naming.lookup("//localhost:8088/EchoServer/master");
                while (true) {
                    objetoRemoto.healthCheck();
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
            Echo master = (Echo) Naming.lookup("//localhost:8088/EchoServer/master");
            var history = master.getListOfMsg();
            echoServer.updateHistory(history);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void electNewMaster() {
        try {
            var registry = LocateRegistry.getRegistry(8088);
            System.out.println(registry.lookup("//localhost:8088/EchoServer/master").toString());
        } catch (RemoteException | NotBoundException e) {
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
