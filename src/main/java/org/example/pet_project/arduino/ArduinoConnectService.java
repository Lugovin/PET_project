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


    @PostConstruct
    public void init() {
        // Подключаемся один раз при старте приложения
        connect();
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
}
