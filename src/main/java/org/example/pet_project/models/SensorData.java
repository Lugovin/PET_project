package org.example.pet_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorData(String sensorId,
                         Double temperature,
                         Double temperatureOutside, // будет null если поля нет в JSON
                         Double humidity,
                         Double pressure,
                         Long messId,
                         LocalDateTime timestamp) {




    public String toMessage() {
        // Конвертируем гПа в мм рт.ст.
        Double pressureMmHg = pressure != null ?
                Math.round(pressure * 0.750062 * 10.0) / 10.0 : null;

        StringBuilder sb = new StringBuilder();
        sb.append("🌡️ *Данные с датчика*\n\n");

        // Температура внутри
        sb.append("🏠 ")
                .append(getTemperatureEmoji(temperature))
                .append(" *Внутри:* ")
                .append(String.format("%.1f°C", temperature != null ? temperature : 0.0))
                .append("\n");

        // Температура снаружи (если есть)
        if (temperatureOutside != null) {
            sb.append("🌍 ")
                    .append(getTemperatureEmoji(temperatureOutside))
                    .append(" *Снаружи:* ")
                    .append(String.format("%.1f°C", temperatureOutside))
                    .append("\n");
        }

        // Влажность и давление
        sb.append("💧 ")
                .append(getHumidityEmoji())
                .append(" *Влажность:* ")
                .append(String.format("%.1f%%", humidity != null ? humidity : 0.0))
                .append("\n")
                .append("🌡️ ")
                .append(getPressureEmoji())
                .append(" *Давление:* ")
                .append(String.format("%.1f мм рт.ст.", pressureMmHg != null ? pressureMmHg : 0.0))
                .append("\n\n");

        // Метаданные
        sb.append("🆔 *Датчик:* ").append(sensorId != null ? sensorId : "unknown").append("\n")
                .append("📨 *Сообщение:* #").append(messId != null ? messId : 0).append("\n")
                .append("🕐 *Время:* ")
                .append(timestamp != null ?
                        timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) :
                        "Нет данных");

        return sb.toString();
    }

    private String getTemperatureEmoji(Double temp) {
        if (temp == null) return "🌡️";
        if (temp < 0) return "❄️";
        if (temp < 10) return "🥶";
        if (temp < 20) return "😊";
        if (temp < 30) return "😎";
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


