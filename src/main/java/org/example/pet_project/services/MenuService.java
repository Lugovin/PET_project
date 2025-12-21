package org.example.pet_project.services;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.example.pet_project.menu.AboutMenu;
import org.example.pet_project.menu.ArduinoAnswerMenu;
import org.example.pet_project.menu.CurrencyMenu;
import org.example.pet_project.menu.HelpMenu;
import org.example.pet_project.menu.MainMenu;
import org.example.pet_project.menu.SettingsMenu;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Фасад/координатор для работы с меню
 */
@Service
public class MenuService {

    private final MenuConfig config;
    private final MainMenu mainMenu;
    private final CurrencyMenu currencyMenu;
    private final SettingsMenu settingsMenu;
    private final HelpMenu helpMenu;
    private final AboutMenu aboutMenu;
    private final ArduinoAnswerMenu arduinoAnswerMenu;

    public MenuService(MenuConfig config,
                       MainMenu mainMenu,
                       CurrencyMenu currencyMenu,
                       SettingsMenu settingsMenu,
                       HelpMenu helpMenu,
                       AboutMenu aboutMenu,
                       ArduinoAnswerMenu arduinoAnswerMenu) {
        this.config = config;
        this.mainMenu = mainMenu;
        this.currencyMenu = currencyMenu;
        this.settingsMenu = settingsMenu;
        this.helpMenu = helpMenu;
        this.aboutMenu = aboutMenu;
        this.arduinoAnswerMenu = arduinoAnswerMenu;
    }

    // Геттеры для констант (чтобы другие классы могли их использовать)
    public String getMainMenuCallback() {
        return MenuConfig.CB_MAIN_MENU;
    }

    public String getCurrencyMenuCallback() {
        return MenuConfig.CB_CURRENCY_MENU;
    }

    public String getCurrencySelectPrefix() {
        return MenuConfig.CB_CURRENCY_SELECT;
    }

    public String getSettingsCallback() {
        return MenuConfig.CB_SETTINGS;
    }

    public String getHelpCallback() {
        return MenuConfig.CB_HELP;
    }

    public String getAboutCallback() {
        return MenuConfig.CB_ABOUT;
    }

    public String getAllCurrenciesCallback() {
        return MenuConfig.CB_ALL_CURRENCIES;
    }

    public String getBackCallback() {
        return MenuConfig.CB_BACK;
    }

    // Основные методы создания меню

    public SendMessage createWelcomeMessage(long chatId, String userName) {
        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.getWelcomeText(userName))
                .build();
    }

    public SendMessage createMainMenu(long chatId) {
        return mainMenu.createMenu(chatId);
    }

    public SendMessage createCurrencyMenu(long chatId) {
        return currencyMenu.createMenu(chatId);
    }

    public SendMessage createCurrencyResultMenu(long chatId, String currencyInfo) {
        return currencyMenu.createResultMenu(chatId, currencyInfo);
    }

    public SendMessage createArduinoAnswerMenu(long chatId, String answer) {
        return arduinoAnswerMenu.createArduinoResponceMenu(chatId, answer);
    }

    public SendMessage createSettingsMenu(long chatId) {
        return settingsMenu.createMenu(chatId);
    }

    public SendMessage createHelpMenu(long chatId) {
        return helpMenu.createMenu(chatId);
    }

    public SendMessage createAboutMenu(long chatId) {
        return aboutMenu.createMenu(chatId);
    }

    public SendMessage createAllCurrenciesMenu(long chatId, String currenciesList) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🔍 Выбрать валюту", MenuConfig.CB_CURRENCY_MENU)
                .button("🔙 Назад", MenuConfig.CB_BACK)
                .endRow()
                .build();

        String formattedText = String.format(MenuConfig.Texts.ALL_CURRENCIES_TITLE, currenciesList);

        return MessageBuilder.create(chatId)
                .text(formattedText)
                .inlineKeyboard(keyboard)
                .build();
    }


    // Вспомогательные методы

    public SendMessage createErrorMessage(long chatId, String message) {
        return MessageBuilder.create(chatId)
                .text("❌ " + message)
                .build();
    }

    public SendMessage createSimpleMessage(long chatId, String text) {
        return MessageBuilder.create(chatId)
                .text(text)
                .build();
    }
}
