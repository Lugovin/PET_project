package org.example.pet_project.services.impl;

import org.example.pet_project.services.ValuteService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


@Service
public class ValuteServiceImpl implements ValuteService {


    @Override
    public String getValuteRateByCode(String valuteCode) throws IOException {

        Map<String, Map<String, Object>> currencyData;  // мапа всех распарсеных курсов валют
        Map<String, Object> data; // распарсеная мапа конкретной валюты

        currencyData = getAndParseValuteList();

        if (currencyData.containsKey(valuteCode)) {
            data = currencyData.get(valuteCode);
            System.out.println("Курс: " + data.get("Value"));
            System.out.println("Название: " + data.get("Name"));
        } else {
            throw new IOException();
        }
        return "Официальный курс рубля:  \n" +
                " " + data.get("Nominal") + " " + valuteCode + " = " + data.get("Value") + " RUB.";
    }

    private static Map<String, Map<String, Object>> getAndParseValuteList() throws IOException {
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        String jsonString = "";
        while (scanner.hasNext()) {
            jsonString += scanner.nextLine();
        }

        JSONObject json = new JSONObject(jsonString);
        JSONObject valuteObject = json.getJSONObject("Valute");

        Map<String, Map<String, Object>> currencyData = new HashMap<>();

        // Сохраняем все данные валюты в Map
        for (String currencyCode : valuteObject.keySet()) {
            JSONObject currencyJson = valuteObject.getJSONObject(currencyCode);

            Map<String, Object> data = new HashMap<>();
            data.put("ID", currencyJson.getString("ID"));
            data.put("NumCode", currencyJson.getString("NumCode"));
            data.put("CharCode", currencyJson.getString("CharCode"));
            data.put("Nominal", currencyJson.getInt("Nominal"));
            data.put("Name", currencyJson.getString("Name"));
            data.put("Value", currencyJson.getDouble("Value"));
            data.put("Previous", currencyJson.getDouble("Previous"));

            currencyData.put(currencyCode, data);
        }
        return currencyData;
    }

}
