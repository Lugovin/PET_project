package org.example.pet_project;


//import org.example.pet_project.arduino.EthernetControlPanel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
public class PetProjectApplication {


    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        // Запускаем Spring
        ConfigurableApplicationContext context =
                SpringApplication.run(PetProjectApplication.class, args);

    }
}
