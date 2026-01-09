package org.example.pet_project.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
/**
 * Фасад/координатор для работы с меню
 */

public interface MenuService {

    // методы создания меню

    SendMessage createWelcomeMessage(long chatId, String userName);

    SendMessage createMainMenu(long chatId);

    SendMessage createClimateMenu(long chatId);

    SendMessage createCurrencyMenu(long chatId);

    SendMessage createCurrencyResultMenu(long chatId, String currencyInfo);

    SendMessage createArduinoAnswerMenu(long chatId, String answer);

    SendMessage createSettingsMenu(long chatId);

    SendMessage createHelpMenu(long chatId);

    SendMessage createAboutMenu(long chatId);

    SendMessage createAllCurrenciesMenu(long chatId, String currenciesList);


    // Вспомогательные методы

    SendMessage createErrorMessage(long chatId, String message);

    SendMessage createSimpleMessage(long chatId, String text);
}
