package org.example.pet_project.arduino;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ArduinoControlApp extends JFrame {
    private ArduinoSerialManager arduinoManager;
    private JTextArea logArea;
    private JComboBox<String> portComboBox;
    private JButton connectButton;
    private JButton ledButton;
    private boolean ledState = false;

    public ArduinoControlApp() {
        arduinoManager = new ArduinoSerialManager();
        //arduinoManager.setDataListener(new ArduinoSerialManager.DataListener() {
        arduinoManager.setDataListener(new DataListener() {
            @Override
            public void onDataReceived(String data) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Получено: " + data + "\n");

                    // Парсинг данных
                    Map<String, Object> parsed = ArduinoDataParser.parseSensorData(data);
                    if (parsed.containsKey("temperature")) {
                        updateTemperature((double) parsed.get("temperature"));
                    }
                });
            }

            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("ОШИБКА: " + error + "\n");
                    JOptionPane.showMessageDialog(
                            ArduinoControlApp.this,
                            error,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }

            @Override
            public void onConnectionStatusChanged(boolean connected) {
                SwingUtilities.invokeLater(() -> {
                    connectButton.setText(connected ? "Отключиться" : "Подключиться");
                    ledButton.setEnabled(connected);
                });
            }
        });

        initUI();
        refreshPorts();
    }

    private void initUI() {
        setTitle("Управление Arduino");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель управления
        JPanel controlPanel = new JPanel(new FlowLayout());

        // Выбор порта
        portComboBox = new JComboBox<>();
        controlPanel.add(new JLabel("Порт:"));
        controlPanel.add(portComboBox);

        // Кнопка обновления портов
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> refreshPorts());
        controlPanel.add(refreshButton);

        // Кнопка подключения
        connectButton = new JButton("Подключиться");
        connectButton.addActionListener(e -> toggleConnection());
        controlPanel.add(connectButton);

        // Кнопка управления светодиодом
        ledButton = new JButton("Светодиод ВКЛ");
        ledButton.setEnabled(false);
        ledButton.addActionListener(e -> toggleLED());
        controlPanel.add(ledButton);

        // Кнопка запроса температуры
        JButton tempButton = new JButton("Запросить температуру");
        tempButton.addActionListener(e -> arduinoManager.requestSensorData(0));
        controlPanel.add(tempButton);

        add(controlPanel, BorderLayout.NORTH);

        // Лог
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Статус бар
        JLabel statusLabel = new JLabel("Не подключено");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void refreshPorts() {
        String[] ports = ArduinoSerialManager.getAvailablePorts();
        portComboBox.removeAllItems();
        for (String port : ports) {
            portComboBox.addItem(port);
        }
    }

    private void toggleConnection() {
        if (arduinoManager.isConnected()) {
            arduinoManager.disconnect();
        } else {
            String selected = (String) portComboBox.getSelectedItem();
            if (selected != null) {
                String portName = selected.split(" - ")[0];
                arduinoManager.connect(portName, 9600);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Выберите порт",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void toggleLED() {
        ledState = !ledState;
        arduinoManager.setDigitalPin(13, ledState);
        ledButton.setText(ledState ? "Светодиод ВЫКЛ" : "Светодиод ВКЛ");
    }

    private void updateTemperature(double temp) {
        // Обновление UI с температурой
        logArea.append(String.format("Температура: %.1f°C\n", temp));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ArduinoControlApp().setVisible(true);
        });
    }
}
