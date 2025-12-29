package org.example.pet_project.models;

import java.time.LocalDateTime;
import java.util.List;

public record SensorData(String sensorId,
                         Double temperature,
                         Double humidity,
                         Double pressure,
                         Long messId,
                         LocalDateTime timestamp) {


    @Override
    public String toString() {
        return "SensorData{" +
                "sensorId='" + sensorId + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", messId=" + messId +
                ", timestamp=" + timestamp +
                '}';
    }

    public String toMessage() {
        // Конвертируем гПа в мм рт.ст. (1 гПа = 0.750062 мм рт.ст.)
        Double pressureMmHg = pressure != null ?
                Math.round(pressure * 0.750062 * 10.0) / 10.0 :  // округляем до 0.1
                null;

        return String.format("""
            🌡️ *Данные с датчика*
            
            %s *Температура:* %.1f°C
            %s *Влажность:* %.1f%%
            %s *Давление:* %.1f мм рт.ст.
            
            🆔 *Датчик:* %s
            📨 *Сообщение:* #%d
            🕐 *Время:* %s
            """,
                getTemperatureEmoji(),
                temperature != null ? temperature : 0.0,
                getHumidityEmoji(),
                humidity != null ? humidity : 0.0,
                getPressureEmoji(),
                pressureMmHg != null ? pressureMmHg : 0.0,
                sensorId != null ? sensorId : "unknown",
                messId != null ? messId : 0,
                timestamp != null ?
                        timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) :
                        "Нет данных"
        );
    }

    private String getTemperatureEmoji() {
        if (temperature == null) return "🌡️";
        if (temperature < 0) return "❄️";
        if (temperature < 10) return "🥶";
        if (temperature < 20) return "😊";
        if (temperature < 30) return "😎";
        return "🔥";
    }

    private String getHumidityEmoji() {
        if (humidity == null) return "💧";
        if (humidity < 30) return "🏜️";
        if (humidity < 60) return "😊";
        return "🌧️";
    }

    private String getPressureEmoji() {
        if (pressure == null) return "🌡️";
        if (pressure < 950) return "🌀";
        if (pressure < 1000) return "🌤️";
        return "☀️";
    }


}


