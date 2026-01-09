package org.example.pet_project.mqtt;




import org.example.pet_project.config.FeignConfig;
import org.example.pet_project.models.SensorData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
@Component
// Feign клиент для общения с MQTT брокером
@FeignClient(name = "mqtt-broker", url = "${mqtt.broker.url}", configuration = FeignConfig.class)
public interface MqttBrokerClient {

    /**
     * Получить данные конкретного датчика
     */
    @GetMapping("/api/bot/sensor/{sensorId}")
    SensorData getSensorData(@PathVariable("sensorId") String sensorId);

    /**
     * Получить все датчики
     */
    @GetMapping("/api/bot/sensors")
    List<SensorData> getAllSensors();





}