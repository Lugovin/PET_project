package org.example.pet_project.arduino;


import org.springframework.stereotype.Service;

@Service
public class ArduinoCommandService {

    private final ArduinoConnectService arduinoConnectService;


    public ArduinoCommandService(ArduinoConnectService arduinoConnectService) {
        this.arduinoConnectService = arduinoConnectService;
    }


    /**
     * Управление реле
     */
    public String setRelay(int relayNumber, boolean state) {
        String command = "RELAY:" + relayNumber + ":" + (state ? "ON" : "OFF");
        System.out.println("RELAY:" + relayNumber + ":" + (state ? "ON" : "OFF"));
        return arduinoConnectService.sendCommand(command);
    }

    /**
     * Чтение датчика
     */
    public String readSensor(String sensorPin) {
        return arduinoConnectService.sendCommand("GET:SENSOR:" + sensorPin);
    }

    /**
     * Чтение всех датчиков
     */
    public String readAllSensors() {
        return arduinoConnectService.sendCommand("GET:ALL");
    }

    /**
     * Сброс всех выходов
     */
    public String resetAll() {
        return arduinoConnectService.sendCommand("RESET");
    }

    /**
     * Получение статуса
     */
    public String getStatus() {
        return arduinoConnectService.sendCommand("STATUS");
    }
}
