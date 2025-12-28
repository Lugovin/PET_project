package org.example.pet_project.mqtt;




import org.example.pet_project.models.SensorData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
@Component
// Feign клиент для общения с MQTT брокером
@FeignClient(name = "mqtt-broker", url = "${mqtt.broker.url:http://localhost:8081}")
public interface MqttBrokerClient {

    /**
     * Получить текущую температуру
     */
    @GetMapping("/api/bot/temperature")
    Map<String, Object> getTemperature(@RequestParam(value = "sensorId", required = false) String sensorId);

    /**
     * Получить текущую влажность
     */
    @GetMapping("/api/bot/humidity")
    Map<String, Object> getHumidity(@RequestParam(value = "sensorId", required = false) String sensorId);

    /**
     * Получить активные алерты
     */
    @GetMapping("/api/bot/alerts")
    Map<String, Object> getAlerts();

    /**
     * Получить статистику
     */
    @GetMapping("/api/bot/stats")
    Map<String, Object> getStats();

    /**
     * Получить данные конкретного датчика
     */
    @GetMapping("/api/bot/sensor")
    Map<String, Object> getSensorData(@RequestParam("sensorId") String sensorId);

    /**
     * Получить все датчики
     */
    @GetMapping("/api/bot/sensors")
    SensorData getAllSensors();


}