package org.example.pet_project.bot;

import org.example.pet_project.models.SensorData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class TelegramMessageFormatter {

    public String formatAllSensors(List<SensorData> sensors) {

        if (sensors == null || sensors.isEmpty()) {
            return "❌ Нет данных от датчиков";
        }

        return "📡 *Показания всех датчиков*\n\n" +
                sensors.stream()
                        .map(SensorData::toMessage)
                        .collect(Collectors.joining("\n────────────────────\n"));
    }
}
