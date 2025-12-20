package org.example.pet_project.arduino;

// ArduinoEthernetClient.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@Component
public class ArduinoEthernetClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String ipAddress;
    private int port;
    private boolean connected = false;
    private ExecutorService executor;
    private ArduinoService arduinoService;

    // Callback интерфейсы
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionError(String error);
    }

    public interface DataListener {
        void onDataReceived(String data);
        void onCommandResponse(String command, String response);
    }

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    public ArduinoEthernetClient(@Value("${arduino.ip:192.168.0.177}") String ip, @Value("${arduino.port:8080}")int port) {
        this.ipAddress = ip;
        this.port = port;
        this.executor = Executors.newFixedThreadPool(2);

        System.out.println("Arduino client создан для IP: " + ipAddress + " : " + port);
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    /**
     * Подключение к Arduino
     */
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), 3000);
            socket.setSoTimeout(5000); // Таймаут чтения 5 секунд

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connected = true;

            // Запуск потока для чтения данных
            startReadingThread();

            if (connectionListener != null) {
                connectionListener.onConnected();
            }

            System.out.println("Подключено к " + ipAddress + ":" + port);
            return true;

        } catch (Exception e) {
            connected = false;
            if (connectionListener != null) {
                connectionListener.onConnectionError(e.getMessage());
            }
            System.err.println("Ошибка подключения: " + e.getMessage());
            return false;
        }
    }

    /**
     * Асинхронное чтение данных
     */
    private void startReadingThread() {
        executor.submit(() -> {
            try {
                String response;
                while (connected && socket != null && !socket.isClosed()) {
                    if ((response = in.readLine()) != null) {
                        if (dataListener != null) {
                            dataListener.onDataReceived(response);
                        }
                        System.out.println("[ARDUINO] " + response);
                    }
                }
            } catch (SocketTimeoutException e) {
                // Таймаут - нормальная ситуация, продолжаем слушать
            } catch (Exception e) {
                if (connected) {
                    System.err.println("Ошибка чтения: " + e.getMessage());
                    disconnect();
                }
            }
        });
    }

    /**
     * Отправка команды с ожиданием ответа
     */
    public String sendCommand(String command) {
        return sendCommand(command, 1000);
    }

    public String sendCommand(String command, int timeoutMs) {
        if (!connected) {
            return "ERROR:NOT_CONNECTED";
        }

        Future<String> future = executor.submit(() -> {
            try {
                // Отправка команды
                out.println(command);

                // Чтение ответа с таймаутом
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    if (in.ready()) {
                        String response = in.readLine();
                        if (response != null) {
                            if (dataListener != null) {
                                dataListener.onCommandResponse(command, response);
                            }
                            return response;
                        }
                    }
                    Thread.sleep(10);
                }
                return "ERROR:TIMEOUT";

            } catch (Exception e) {
                return "ERROR:" + e.getMessage();
            }
        });

        try {
            return future.get(timeoutMs + 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Управление реле
     */
    public String setRelay(int relayNumber, boolean state) {
        String command = "RELAY:" + relayNumber + ":" + (state ? "ON" : "OFF");
        System.out.println("RELAY:" + relayNumber + ":" + (state ? "ON" : "OFF"));
        return arduinoService.sendCommand(command);
    }

    /**
     * Чтение датчика
     */
    public String readSensor(String sensorPin) {
        return arduinoService.sendCommand("GET:SENSOR:" + sensorPin);
    }

    /**
     * Чтение всех датчиков
     */
    public String readAllSensors() {
        return arduinoService.sendCommand("GET:ALL");
    }

    /**
     * Сброс всех выходов
     */
    public String resetAll() {
        return arduinoService.sendCommand("RESET");
    }

    /**
     * Получение статуса
     */
    public String getStatus() {
        return arduinoService.sendCommand("STATUS");
    }

    /**
     * Асинхронная отправка команды
     */
    public void sendCommandAsync(String command) {
        executor.submit(() -> {
            try {
                out.println(command);
                System.out.println("[SENT] " + command);
            } catch (Exception e) {
                System.err.println("Ошибка отправки: " + e.getMessage());
            }
        });
    }

    /**
     * Отключение
     */
    public void disconnect() {
        connected = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }

        if (connectionListener != null) {
            connectionListener.onDisconnected();
        }

        executor.shutdown();
        System.out.println("Отключено от " + ipAddress);
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Поиск Arduino в сети (UDP broadcast)
     */
    public static String discoverArduino(int timeoutMs) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            // Отправка broadcast сообщения
            String discoveryMessage = "ARDUINO_DISCOVERY";
            byte[] sendData = discoveryMessage.getBytes();

            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    broadcastAddress,
                    8888
            );

            socket.send(sendPacket);

            // Ожидание ответа
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            socket.close();

            if (response.startsWith("ARDUINO_FOUND")) {
                return receivePacket.getAddress().getHostAddress();
            }

        } catch (Exception e) {
            System.out.println("Не удалось найти Arduino: " + e.getMessage());
        }

        return null;
    }
}
