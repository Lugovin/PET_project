package org.example.pet_project.services;


import java.io.IOException;
import java.util.Map;

public interface ValuteService {

    String getValuteRateByCode(String valuteCode) throws IOException;

    Map<String, String> getAllCurrencies() throws IOException;
}
