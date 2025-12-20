package org.example.pet_project.arduino;

import java.util.HashMap;
import java.util.Map;

public class ArduinoDataParser {

    /**
     * Парсинг данных от Arduino
     * Пример форматов:
     * TEMP:25.5
     * HUM:60.2
     * DIGITAL:13:1
     * ANALOG:A0:512
     */
    public static Map<String, Object> parseSensorData(String rawData) {
        Map<String, Object> data = new HashMap<>();

        // Разделяем строку по переводам строк
        String[] lines = rawData.split("\n|\r");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                if (line.startsWith("TEMP:")) {
                    double temp = Double.parseDouble(line.substring(5));
                    data.put("temperature", temp);
                }
                else if (line.startsWith("HUM:")) {
                    double hum = Double.parseDouble(line.substring(4));
                    data.put("humidity", hum);
                }
                else if (line.startsWith("DIGITAL:")) {
                    String[] parts = line.split(":");
                    int pin = Integer.parseInt(parts[1]);
                    int value = Integer.parseInt(parts[2]);
                    data.put("digital_" + pin, value);
                }
                else if (line.startsWith("ANALOG:")) {
                    String[] parts = line.split(":");
                    String pin = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    data.put("analog_" + pin, value);
                }
                else if (line.startsWith("ERROR:")) {
                    data.put("error", line.substring(6));
                }
            } catch (Exception e) {
                System.err.println("Ошибка парсинга: " + line);
            }
        }

        return data;
    }
}