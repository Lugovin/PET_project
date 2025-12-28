package org.example.pet_project.services;



import org.example.pet_project.mqtt.MqttBrokerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MqttService {

    @Autowired
    private MqttBrokerClient mqttBrokerClient;

    /**
     * Получить температуру для показа в Telegram
     */
    public String getTemperatureMessage(String sensorId) {
        try {
            Map<String, Object> response = mqttBrokerClient.getTemperature(sensorId);

            if ("success".equals(response.get("status"))) {
                if (sensorId != null) {
                    // Данные конкретного датчика
                    Double temp = (Double) response.get("temperature");
                    String location = (String) response.get("location");
                    String timestamp = (String) response.get("timestamp");

                    return String.format(
                            "🌡️ Температура в %s\n" +
                                    "Температура: %.1f°C\n" +
                                    "Время: %s",
                            location != null ? location : sensorId,
                            temp,
                            formatTime(timestamp)
                    );
                } else {
                    // Все датчики
                    return formatAllTemperatures(response);
                }
            } else {
                return "❌ Нет данных от датчиков";
            }

        } catch (Exception e) {
            return "❌ Ошибка получения данных: " + e.getMessage();
        }
    }

    /**
     * Получить алерты
     */
    public String getAlertsMessage() {
        try {
            Map<String, Object> response = mqttBrokerClient.getAlerts();

            if ("success".equals(response.get("status"))) {
                Integer count = (Integer) response.get("count");
                if (count != null && count > 0) {
                    return formatAlerts(response);
                } else {
                    return "✅ Все датчики в норме";
                }
            } else {
                return "❌ Нет данных об алертах";
            }

        } catch (Exception e) {
            return "❌ Ошибка получения алертов: " + e.getMessage();
        }
    }
//
//    /**
//     * Проверить доступность брокера
//     */
//    public boolean isBrokerAvailable() {
//        try {
//            Map<String, Object> health = mqttBrokerClient.healthCheck();
//            return "UP".equals(health.get("status"));
//        } catch (Exception e) {
//            return false;
//        }
//    }

    private String formatTime(String timestamp) {
        // Простая форматировка времени
        if (timestamp != null && timestamp.length() > 10) {
            return timestamp.substring(11, 16); // Берем только часы:минуты
        }
        return timestamp;
    }

    private String formatAllTemperatures(Map<String, Object> response) {
        StringBuilder sb = new StringBuilder();
        sb.append("🌡️ Температуры по датчикам:\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> temps = (List<Map<String, Object>>) response.get("temperatures");

        if (temps != null) {
            for (Map<String, Object> temp : temps) {
                sb.append(String.format("📍 %s: %.1f°C (%s)\n",
                        temp.get("location") != null ? temp.get("location") : temp.get("sensorId"),
                        temp.get("temperature"),
                        formatTime((String) temp.get("timestamp"))
                ));
            }
        }

        return sb.toString();
    }

    private String formatAlerts(Map<String, Object> response) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 КРИТИЧЕСКИЕ СОБЫТИЯ:\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alerts = (List<Map<String, Object>>) response.get("alerts");

        if (alerts != null) {
            for (Map<String, Object> alert : alerts) {
                String type = (String) alert.get("type");
                String status = (String) alert.get("status");
                Double value = (Double) alert.get("value");
                String sensorId = (String) alert.get("sensorId");

                String emoji = "⚠️";
                if ("HIGH".equals(status) && "temperature".equals(type)) {
                    emoji = "🔥";
                } else if ("LOW".equals(status) && "temperature".equals(type)) {
                    emoji = "❄️";
                }

                sb.append(String.format("%s %s в %s: %.1f%s\n",
                        emoji,
                        "temperature".equals(type) ? "Температура" : "Влажность",
                        sensorId,
                        value,
                        "temperature".equals(type) ? "°C" : "%"
                ));
            }
        }

        return sb.toString();
    }
}