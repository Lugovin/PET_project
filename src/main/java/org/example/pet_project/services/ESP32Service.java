package org.example.pet_project.services;

import org.example.pet_project.mqtt.MqttBrokerClient;
import org.springframework.stereotype.Service;

@Service
public class ESP32Service {

    private MqttBrokerClient mqttBrokerClient;

    public ESP32Service(MqttBrokerClient mqttBrokerClient) {
        this.mqttBrokerClient = mqttBrokerClient;
    }

    public String getAllSensorsData() {
        return mqttBrokerClient.getAllSensors().toMessage();
    }
}
