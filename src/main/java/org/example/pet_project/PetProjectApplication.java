package org.example.pet_project;


import org.example.pet_project.arduino.EthernetControlPanel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PetProjectApplication {


    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        // Запускаем Spring
        ConfigurableApplicationContext context =
                SpringApplication.run(PetProjectApplication.class, args);

        // Получаем GUI bean из контекста
        EthernetControlPanel gui = context.getBean(EthernetControlPanel.class);

        // Показываем GUI
        SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
        });
    }
}
