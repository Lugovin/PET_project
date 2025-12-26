package org.example.pet_project.bot;



import org.example.pet_project.services.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MqttCommands {

    @Autowired
    private MqttService mqttService;

    public SendMessage handleUpdate(Update update) {
        Message message = update.getMessage();
        String text = message.getText();

        SendMessage response = new SendMessage();
        response.setChatId(message.getChatId().toString());

        switch (text) {
            case "/start":
                response.setText("🤖 Бот мониторинга температуры\n" +
                        "Доступные команды:\n" +
                        "/temp - текущая температура\n" +
                        "/temp room1 - температура в комнате 1\n" +
                        "/alerts - критические события\n" +
                        "/status - статус системы");
                break;

            case "/temp":
                response.setText(mqttService.getTemperatureMessage(null));
                break;

            case "/alerts":
                response.setText(mqttService.getAlertsMessage());
                break;

            case "/status":
                boolean brokerAvailable = mqttService.isBrokerAvailable();
                response.setText(brokerAvailable ?
                        "✅ Система работает нормально\nMQTT брокер доступен" :
                        "❌ MQTT брокер недоступен");
                break;

            default:
                if (text.startsWith("/temp ")) {
                    String sensorId = text.substring(6).trim();
                    response.setText(mqttService.getTemperatureMessage(sensorId));
                } else {
                    response.setText("Неизвестная команда. Используйте /start для списка команд");
                }
        }

        return response;
    }
}