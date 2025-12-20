package org.example.pet_project.arduino;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// ArduinoService.java - ЕДИНСТВЕННЫЙ сервис для работы с Arduino
@Service
public class ArduinoService {



    @Autowired
    private ArduinoEthernetClient arduinoEthernetClient;
    private boolean connected = false;
    private final Object lock = new Object(); // для потокобезопасности

    @PostConstruct
    public void init() {
        // Подключаемся один раз при старте приложения
        connect();
    }

    public synchronized boolean connect() {
        if (connected) return true;

        try {
            //arduinoClient = new ArduinoEthernetClient(ipAddress,port);
            connected = arduinoEthernetClient.connect();
            return connected;
        } catch (Exception e) {
            System.err.println("Ошибка подключения к Arduino: " + e.getMessage());
            return false;
        }
    }

    /**
     * ЕДИНСТВЕННЫЙ метод для отправки команд (потокобезопасный)
     */
    public String sendCommand(String command) {
        synchronized (lock) {
            if (!connected) {
                return "ERROR: Arduino не подключена";
            }

            try {
                return arduinoEthernetClient.sendCommand(command);
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }
    }

    /**
     * Метод для Telegram бота
     */
    public String setRelay(int relayNumber, boolean state) {
        String command = String.format("RELAY:%d:%s",
                relayNumber, state ? "ON" : "OFF");
        return sendCommand(command);
    }

    /**
     * Метод для GUI
     */
    public String readSensors() {
        return sendCommand("GET:ALL");
    }

    public void disconnect() {
        synchronized (lock) {
            if (arduinoEthernetClient != null) {
                arduinoEthernetClient.disconnect();
            }
            connected = false;
        }
    }
}
