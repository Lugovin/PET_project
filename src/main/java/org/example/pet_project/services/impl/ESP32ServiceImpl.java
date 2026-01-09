package org.example.pet_project.services.impl;

import org.example.pet_project.bot.TelegramMessageFormatter;
import org.example.pet_project.models.SensorData;
import org.example.pet_project.mqtt.MqttBrokerClient;
import org.example.pet_project.mqtt.SensorNotFoundException;
import org.example.pet_project.services.ESP32Service;
import org.springframework.stereotype.Service;

@Service
public class ESP32ServiceImpl implements ESP32Service {

    private MqttBrokerClient mqttBrokerClient;
    private TelegramMessageFormatter telegramMessageFormatter;


    public ESP32ServiceImpl(MqttBrokerClient mqttBrokerClient, TelegramMessageFormatter telegramMessageFormatter) {
        this.mqttBrokerClient = mqttBrokerClient;
        this.telegramMessageFormatter = telegramMessageFormatter;
    }

    public String getAllSensorsData() {
        return telegramMessageFormatter.formatAllSensors(mqttBrokerClient.getAllSensors());
    }

    public String getSensorData(String sensorId) {
        try {
            SensorData data = mqttBrokerClient.getSensorData(sensorId);
            return data.toMessage();
        } catch (SensorNotFoundException e) {
            return e.getMessage();
        }
    }
}
