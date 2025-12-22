package org.example.pet_project.arduino;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Service
public class ArduinoConnectService {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String ipAddress;
    private int port;
    private boolean connected = false;
    private ExecutorService executor;


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

    private ArduinoConnectService.ConnectionListener connectionListener;
    private ArduinoConnectService.DataListener dataListener;



    public ArduinoConnectService(@Value("${arduino.ip:192.168.0.177}") String ip, @Value("${arduino.port:8080}")int port) {
        this.ipAddress = ip;
        this.port = port;
        this.executor = Executors.newFixedThreadPool(2);

        System.out.println("Arduino client создан для IP: " + ipAddress + " : " + port);
    }

    // Добавляем методы для внешнего управления
    public boolean isConnected() {
        return connected;
    }

    public String getConnectionStatus() {
        if (connected) {
            return "✅ Подключено к " + ipAddress + ":" + port;
        } else {
            return "❌ Не подключено";
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    // Убираем авто-коннект в @PostConstruct
    @PostConstruct
    public void init() {
        // Не подключаемся автоматически, ждем команды от пользователя
        System.out.println("Arduino сервис инициализирован. Ожидание команды подключения...");
    }


//    @PostConstruct
//    public void init() {
//        // Подключаемся один раз при старте приложения
//        connect();
//    }

    /**
     * Подключение к Arduino
     */
    public String connect() {
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
            return "Arduino is connected!";

        } catch (Exception e) {
            connected = false;
            if (connectionListener != null) {
                connectionListener.onConnectionError(e.getMessage());
            }
            System.err.println("Ошибка подключения: " + e.getMessage());
            return "Can not connect to Arduino.";
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
     * Отключение
     */
    public String disconnect() {
        connected = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
            return "ERROR! NOT DISCONNECTED!";
        }

        if (connectionListener != null) {
            connectionListener.onDisconnected();
        }

        //executor.shutdown();
        System.out.println("Отключено от " + ipAddress);
        return "Arduino is DISCONNECTED!";
    }


    public String sendCommand(String command) {
        return sendCommand(command, 3000);
    }

    public String sendCommand(String command, int timeoutMs) {
        if (!connected) return "ERROR:ARDUINO NOT CONNECTED";

        synchronized (this) { // Защита от параллельных отправок
            try {
                socket.setSoTimeout(timeoutMs);
                out.println(command);
                String response = in.readLine();

                if (dataListener != null) {
                    dataListener.onCommandResponse(command, response);
                }
                return response;

            } catch (SocketTimeoutException e) {
                return "ERROR:TIMEOUT";
            } catch (Exception e) {
                disconnect(); // При ошибке разрываем соединение
                return "ERROR:" + e.getMessage();
            } finally {
                try {
                    socket.setSoTimeout(5000); // Возвращаем стандартный таймаут
                } catch (Exception e) {
                    // Игнорируем
                }
            }
        }
    }
}
