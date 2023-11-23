package org.mqtt.servers;

import org.mqtt.echo.Echo;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientApp {
    static int i = 0;
    public static void main(String[] args) throws InterruptedException {
        Echo remoteEcho;
        try {
            remoteEcho = (Echo) Naming.lookup("//localhost:8088/Echo/master");
            while(true){
                i++;
                System.out.println(remoteEcho.echo(String.format("Teste %d", i)));
                if(i%5==0){
                    System.out.println(remoteEcho.getListOfMsg());
                }
                Thread.sleep(2000);
            }
        } catch (MalformedURLException | NotBoundException | RemoteException | InterruptedException e) {
            i--;
            Thread.sleep(5000);
            main(args);
        }
    }
}
