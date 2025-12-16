package org.example.pet_project.models;

import lombok.Data;

@Data
public class ValuteModelRus {
    private String id;
    private String numCode;
    private String charCode;
    private int nominal;
    private String name;
    private double value;
    private double previous;

    @Override
    public String toString() {
        return String.format("%s (Номинал: %d, Курс: %.4f)", name, nominal, value);
    }
}
