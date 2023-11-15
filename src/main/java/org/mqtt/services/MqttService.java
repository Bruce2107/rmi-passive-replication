package org.mqtt.services;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MqttService {
    public static String topic = "mqtt-topic";
    //Quality of Service = 2 offers the highest level of service in MQTT
    public static int qos = 2;
    public static String broker = "tcp://127.0.0.1:1883";
    public static String publisherId = "mqtt-publisher";
    private final MqttClient mqttPublisher;

    public MqttService(String clientId){
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttPublisher = new MqttClient(broker, UUID.randomUUID().toString(), persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);
            System.out.println("Connecting to broker: " + broker);
            mqttPublisher.connect(connOpts);
            System.out.println("Connected");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(){
        subscribe(topic);
    }

    public void subscribe(String topic){
        try {
            System.out.println("Subscribing to topic " + topic);
            mqttPublisher.subscribe(topic);
            System.out.println("Subscribded");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    public void publish(String message){
        publish(topic, message);
    }

    public void publish(String topic, String message){
        try {
            var mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            System.out.println("Publishing message to topic " + topic + " :" + message);
            mqttPublisher.publish(topic, mqttMessage);
            System.out.println("Published");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMqttCallBack(MqttCallback callBack){
        mqttPublisher.setCallback(callBack);
    }
}
