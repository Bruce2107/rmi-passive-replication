package org.mqtt.servers;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

public class RMIInstance {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(8088);
            System.out.println("Registry iniciado na porta 8088");
            while (true) {

            }
        } catch (ExportException e) {
            System.out.println("Registry já iniciado!");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
}
