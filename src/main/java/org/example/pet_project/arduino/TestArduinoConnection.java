package org.example.pet_project.arduino;

public class TestArduinoConnection {
    public static void main(String[] args) {
        ArduinoSerialManager manager = new ArduinoSerialManager();

        manager.setDataListener(new DataListener() {
            @Override
            public void onDataReceived(String data) {
                System.out.println(">> " + data);
            }

            @Override
            public void onError(String error) {
                System.err.println("ERROR: " + error);
            }

            @Override
            public void onConnectionStatusChanged(boolean connected) {
                System.out.println("Connection: " + (connected ? "CONNECTED" : "DISCONNECTED"));
            }
        });

        // Автопоиск Arduino
        System.out.println("Поиск Arduino...");
        boolean connected = manager.autoConnect();

        if (connected) {
            System.out.println("Подключено успешно!");

            // Тестируем команды
            try {
                Thread.sleep(1000); // Ждем инициализации

                System.out.println("Включение светодиода...");
                manager.sendCommand("LED_ON");
                Thread.sleep(2000);

                System.out.println("Выключение светодиода...");
                manager.sendCommand("LED_OFF");
                Thread.sleep(1000);

                System.out.println("Запрос температуры...");
                manager.sendCommand("GET_TEMP");
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Оставляем подключение на 10 секунд
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            manager.disconnect();
        } else {
            System.out.println("Не удалось подключиться. Доступные порты:");
            String[] ports = ArduinoSerialManager.getAvailablePorts();
            for (String port : ports) {
                System.out.println("  - " + port);
            }
        }
    }
}
