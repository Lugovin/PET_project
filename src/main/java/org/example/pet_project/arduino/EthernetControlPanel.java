package org.example.pet_project.arduino;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@Component
public class EthernetControlPanel extends JFrame {

    @Autowired
    private ArduinoService arduinoService; // Spring инжектит здесь

    // Компоненты интерфейса
    private JLabel statusLabel;
    private JTextArea logArea;
    private JButton[] relayButtons = new JButton[8];
    private JLabel[] sensorLabels = new JLabel[6];
    private JButton connectButton;
    private JButton refreshButton;
    private JButton clearLogButton;

    private boolean[] relayStates = new boolean[8];
    private ScheduledExecutorService scheduler;
    private Timer autoRefreshTimer;

    // Флаг инициализации
    private volatile boolean serviceReady = false;

    // Цвета
    private final Color CONNECTED_COLOR = new Color(46, 204, 113);
    private final Color DISCONNECTED_COLOR = new Color(231, 76, 60);
    private final Color RELAY_ON_COLOR = new Color(52, 152, 219);
    private final Color RELAY_OFF_COLOR = new Color(149, 165, 166);
    private final Color WAITING_COLOR = new Color(241, 196, 15);

    public EthernetControlPanel() {
        // Конструктор ТОЛЬКО создает компоненты, не использует arduinoService!
        log("Создание GUI компонентов...");
        initComponents();
        setupLayout();

        setTitle("Управление Arduino через ArduinoService");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Показываем сообщение об ожидании
        statusLabel.setText("⏳ Ожидание инициализации сервиса...");
        statusLabel.setBackground(WAITING_COLOR);
        statusLabel.setForeground(Color.BLACK);

        log("GUI компоненты созданы. Ожидание ArduinoService...");
    }

    /**
     * Этот метод вызывается Spring ПОСЛЕ инъекции зависимостей
     */
    @PostConstruct
    public void initAfterDI() {
        log("Spring инжектировал зависимости");

        if (arduinoService == null) {
            log("❌ ОШИБКА: ArduinoService все еще null!");
            statusLabel.setText("❌ Ошибка инициализации ArduinoService");
            statusLabel.setBackground(DISCONNECTED_COLOR);
            return;
        }

        log("✅ ArduinoService получен: " + arduinoService.getClass().getName());
        serviceReady = true;

        // Теперь безопасно настраиваем остальное
        SwingUtilities.invokeLater(() -> {
            setupEventListeners();
            setupAutoRefresh();
            checkConnection();

            // Показываем окно
            setVisible(true);
            log("✅ GUI полностью инициализирован и отображен");
        });
    }

    private void initComponents() {
        // Статус бар
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setOpaque(true);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setPreferredSize(new Dimension(400, 30));

        // Кнопки управления подключением
        connectButton = new JButton("Переподключиться");
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        connectButton.setEnabled(false); // Отключаем до инициализации

        refreshButton = new JButton("Обновить данные");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshButton.setEnabled(false);

        clearLogButton = new JButton("Очистить лог");
        clearLogButton.setFont(new Font("Arial", Font.PLAIN, 12));

        // Кнопки реле
        for (int i = 0; i < 8; i++) {
            relayButtons[i] = new JButton("Реле " + (i + 1) + " - ВЫКЛ");
            relayButtons[i].setFont(new Font("Arial", Font.BOLD, 12));
            relayButtons[i].setBackground(RELAY_OFF_COLOR);
            relayButtons[i].setForeground(Color.WHITE);
            relayButtons[i].setFocusPainted(false);
            relayButtons[i].setEnabled(false); // Отключаем до инициализации
        }

        // Метки датчиков
        for (int i = 0; i < 6; i++) {
            sensorLabels[i] = new JLabel("A" + i + ": ---");
            sensorLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 14));
            sensorLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        // Лог
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 1. Верхняя панель (статус и управление)
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(connectButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearLogButton);

        topPanel.add(statusPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        add(topPanel, BorderLayout.NORTH);

        // 2. Центральная панель
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Левая панель - управление реле
        JPanel relayPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        relayPanel.setBorder(BorderFactory.createTitledBorder("Управление реле"));

        for (JButton relayButton : relayButtons) {
            relayPanel.add(relayButton);
        }

        // Правая панель - датчики
        JPanel sensorPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        sensorPanel.setBorder(BorderFactory.createTitledBorder("Показания датчиков"));

        for (JLabel sensorLabel : sensorLabels) {
            sensorPanel.add(sensorLabel);
        }

        centerPanel.add(relayPanel);
        centerPanel.add(sensorPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Нижняя панель (лог)
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Лог сообщений"));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        logPanel.add(scrollPane, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        // Кнопка подключения
        connectButton.addActionListener(e -> {
            if (arduinoService == null) {
                log("❌ ArduinoService не доступен");
                return;
            }

            log("Попытка переподключения...");
            boolean connected = arduinoService.connect();
            updateConnectionStatus(connected);
        });

        // Кнопка обновления данных
        refreshButton.addActionListener(e -> {
            if (arduinoService == null) {
                log("❌ ArduinoService не доступен");
                return;
            }

            refreshSensorData();
            //refreshRelayStatus();
        });

        // Кнопка очистки лога
        clearLogButton.addActionListener(e -> logArea.setText(""));

        // Кнопки реле
        for (int i = 0; i < relayButtons.length; i++) {
            final int relayNumber = i;
            relayButtons[i].addActionListener(e -> {
                if (arduinoService == null) {
                    log("❌ ArduinoService не доступен");
                    return;
                }

                toggleRelay(relayNumber);
            });
        }

        // Обработка закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void setupAutoRefresh() {
        // Сначала одноразовый таймер для начальной проверки
        Timer initTimer = new Timer(100, e -> {
            if (arduinoService != null) {
                checkConnection();
                ((Timer) e.getSource()).stop();
            } else {
                log("⏳ Ожидание ArduinoService...");
            }
        });
        initTimer.setRepeats(true);
        initTimer.start();

        // Автообновление данных каждые 3 секунды
        autoRefreshTimer = new Timer(3000, e -> {
            if (arduinoService != null && arduinoService.connect()) {
                refreshSensorData();
            }
        });
        autoRefreshTimer.start();

        // Планировщик для периодических задач
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                if (arduinoService != null) {
                    checkConnection();
                }
            });
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void checkConnection() {
        if (arduinoService == null) {
            log("⚠️ ArduinoService еще не инициализирован");
            return;
        }

        boolean isConnected = arduinoService.connect();
        updateConnectionStatus(isConnected);

        if (isConnected) {
            //refreshRelayStatus();
            refreshSensorData();
        }
    }

    private void updateConnectionStatus(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("✅ Подключено к Arduino");
                statusLabel.setBackground(CONNECTED_COLOR);
                statusLabel.setForeground(Color.WHITE);
                connectButton.setText("Переподключиться");
                enableControls(true);
                log("Успешно подключено к Arduino");
            } else {
                statusLabel.setText("❌ Не подключено к Arduino");
                statusLabel.setBackground(DISCONNECTED_COLOR);
                statusLabel.setForeground(Color.WHITE);
                connectButton.setText("Подключиться");
                enableControls(false);
                log("Ошибка подключения к Arduino");
            }
        });
    }

    private void enableControls(boolean enabled) {
        for (JButton relayButton : relayButtons) {
            relayButton.setEnabled(enabled);
        }
        connectButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    private void toggleRelay(int relayNumber) {
        boolean newState = !relayStates[relayNumber];

        log("Отправка команды: Реле " + (relayNumber + 1) + " -> " +
                (newState ? "ВКЛ" : "ВЫКЛ"));

        String result = arduinoService.setRelay(relayNumber, newState);

        log("Результат: " + result);

        if (result.contains("OK") || result.contains("ON") || result.contains("OFF")) {
            relayStates[relayNumber] = newState;
            updateRelayButton(relayNumber, newState);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ошибка управления реле: " + result,
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRelayButton(int relayNumber, boolean state) {
        SwingUtilities.invokeLater(() -> {
            String text = "Реле " + (relayNumber + 1) + " - " +
                    (state ? "ВКЛ" : "ВЫКЛ");
            relayButtons[relayNumber].setText(text);
            relayButtons[relayNumber].setBackground(
                    state ? RELAY_ON_COLOR : RELAY_OFF_COLOR
            );
        });
    }

//    private void refreshRelayStatus() {
//        if (arduinoService == null) return;
//
//        String status = arduinoService.getRelayStatus();
//
//        if (status != null && status.startsWith("STATUS:")) {
//            String statusBits = status.substring(7);
//            if (statusBits.length() >= 8) {
//                for (int i = 0; i < 8; i++) {
//                    relayStates[i] = statusBits.charAt(i) == '1';
//                    updateRelayButton(i, relayStates[i]);
//                }
//            }
//        }
//    }

    private void refreshSensorData() {
        if (arduinoService == null) return;

        String sensorData = arduinoService.readSensors();

        if (sensorData != null && sensorData.startsWith("ALL_DATA:")) {
            String data = sensorData.substring(9);
            String[] sensors = data.split(",");

            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < Math.min(sensors.length, 6); i++) {
                    String sensorValue = sensors[i].trim();

                    try {
                        String[] parts = sensorValue.split(":");
                        if (parts.length >= 2) {
                            String sensorName = parts[0];
                            int value = Integer.parseInt(parts[1]);

                            String formattedValue;
                            if (sensorName.startsWith("A")) {
                                double voltage = value * (5.0 / 1023.0);
                                formattedValue = String.format("%s: %d (%.2fV)",
                                        sensorName, value, voltage);
                            } else {
                                formattedValue = sensorName + ": " + value;
                            }

                            sensorLabels[i].setText(formattedValue);

                            // Изменяем цвет в зависимости от значения
                            if (value > 800) {
                                sensorLabels[i].setForeground(Color.RED);
                            } else if (value > 400) {
                                sensorLabels[i].setForeground(Color.ORANGE);
                            } else {
                                sensorLabels[i].setForeground(Color.BLACK);
                            }
                        }
                    } catch (NumberFormatException e) {
                        sensorLabels[i].setText("A" + i + ": Ошибка чтения");
                        sensorLabels[i].setForeground(Color.RED);
                    }
                }
            });

            log("Данные датчиков обновлены");
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");

            // Автопрокрутка вниз
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void cleanup() {
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log("GUI закрыт");
    }
}