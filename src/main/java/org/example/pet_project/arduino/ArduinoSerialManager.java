package org.example.pet_project.arduino;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoSerialManager {
    private SerialPort serialPort;
    private boolean connected = false;
    private Thread readThread;
    private StringBuilder inputBuffer = new StringBuilder();
    private DataListener dataListener;

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    /**
     * Поиск и подключение к Arduino
     */
    public boolean connect(String portName, int baudRate) {
        if (connected) {
            disconnect();
        }

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setComPortParameters(baudRate, 8, 1, 0); // 8N1
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                100,
                0
        );

        if (serialPort.openPort()) {
            connected = true;
            startReadingThread();

            if (dataListener != null) {
                dataListener.onConnectionStatusChanged(true);
            }

            System.out.println("Подключено к " + portName);
            return true;
        } else {
            if (dataListener != null) {
                dataListener.onError("Не удалось открыть порт: " + portName);
            }
            return false;
        }
    }

    /**
     * Автопоиск Arduino (по VID/PID)
     */
    public boolean autoConnect() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            // Arduino Mega имеет VID=0x2341, PID=0x0042
            if (port.getDescriptivePortName().contains("Arduino") ||
                    port.getDescriptivePortName().contains("Mega")) {
                return connect(port.getSystemPortName(), 9600);
            }
        }

        // Если не нашли по имени, пробуем все порты
        for (SerialPort port : ports) {
            System.out.println("Порт: " + port.getSystemPortName() +
                    " - " + port.getDescriptivePortName());
        }

        return false;
    }

    /**
     * Запуск потока для чтения данных
     */
    private void startReadingThread() {
        readThread = new Thread(() -> {
            byte[] buffer = new byte[1024];

            while (connected && serialPort != null) {
                try {
                    int bytesAvailable = serialPort.bytesAvailable();
                    if (bytesAvailable > 0) {
                        // Читаем не более размера буфера
                        int toRead = Math.min(bytesAvailable, buffer.length);
                        int numRead = serialPort.readBytes(buffer, toRead);

                        if (numRead > 0) {
                            String chunk = new String(buffer, 0, numRead, "UTF-8");
                            processIncomingData(chunk);
                        }
                    }
                    Thread.sleep(1); // Минимальная задержка
                } catch (Exception e) {
                    if (dataListener != null) {
                        dataListener.onError("Ошибка чтения: " + e.getMessage());
                    }
                }
            }
        });

        readThread.setDaemon(true);
        readThread.start();
    }

    private void processIncomingData(String chunk) {
        inputBuffer.append(chunk);

        String data = inputBuffer.toString();
        int lineEnd;

        // Обрабатываем все полные строки
        while ((lineEnd = data.indexOf('\n')) != -1) {
            String line = data.substring(0, lineEnd).replace("\r", "").trim();
            data = data.substring(lineEnd + 1);

            if (!line.isEmpty()) {
                if (dataListener != null) {
                    dataListener.onDataReceived(line);
                }
                System.out.println("[ARDUINO] " + line);
            }
        }

        // Сохраняем остаток
        inputBuffer = new StringBuilder(data);
    }

    /**
     * Отправка команды на Arduino
     */
    public boolean sendCommand(String command) {
        if (!connected || serialPort == null) {
            if (dataListener != null) {
                dataListener.onError("Не подключено к Arduino");
            }
            return false;
        }

        // Добавляем символ новой строки для разделения команд
        String fullCommand = command + "\n";
        byte[] bytes = fullCommand.getBytes();

        int bytesWritten = serialPort.writeBytes(bytes, bytes.length);

        if (bytesWritten == bytes.length) {
            System.out.println("Отправлено: " + command);
            return true;
        } else {
            if (dataListener != null) {
                dataListener.onError("Ошибка отправки команды");
            }
            return false;
        }
    }

    /**
     * Управление выходами (реле, светодиоды и т.д.)
     */
    public boolean setDigitalPin(int pin, boolean state) {
        String command = String.format("D%d:%d", pin, state ? 1 : 0);
        return sendCommand(command);
    }

    public boolean setAnalogPin(int pin, int value) {
        // value: 0-255 для ШИМ
        String command = String.format("A%d:%d", pin, Math.min(255, Math.max(0, value)));
        return sendCommand(command);
    }

    /**
     * Запрос данных с датчиков
     */
    public boolean requestSensorData(int sensorPin) {
        String command = String.format("SENSOR:%d", sensorPin);
        return sendCommand(command);
    }

    /**
     * Отключение
     */
    public void disconnect() {
        connected = false;

        if (readThread != null && readThread.isAlive()) {
            try {
                readThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }

        if (dataListener != null) {
            dataListener.onConnectionStatusChanged(false);
        }

        System.out.println("Отключено от Arduino");
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Получение списка доступных портов
     */
    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];

        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName() + " - " +
                    ports[i].getDescriptivePortName();
        }

        return portNames;
    }
}