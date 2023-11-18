package org.mqtt.servers;

import org.mqtt.echo.Echo;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientApp {
    public static void main(String[] args) {
        Echo remoteEcho;
        try {
            remoteEcho = (Echo) Naming.lookup("//localhost:8088/Echo/master");
            int i = 0;
            while(true){
                i++;
                System.out.println(remoteEcho.echo(String.format("Teste %d", i)));
                if(i%5==0){
                    System.out.println(remoteEcho.getListOfMsg());
                }
                Thread.sleep(2000);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
