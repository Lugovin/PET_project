package org.example.pet_project.services.impl;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.example.pet_project.menu.AboutMenu;
import org.example.pet_project.menu.ClimatMenu;
import org.example.pet_project.menu.CurrencyMenu;
import org.example.pet_project.menu.HelpMenu;
import org.example.pet_project.menu.MainMenu;
import org.example.pet_project.menu.ResponceMenu;
import org.example.pet_project.menu.SettingsMenu;
import org.example.pet_project.services.MenuService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


@Service
public class MenuServiceImpl implements MenuService {

    private final MainMenu mainMenu;
    private final CurrencyMenu currencyMenu;
    private final SettingsMenu settingsMenu;
    private final HelpMenu helpMenu;
    private final AboutMenu aboutMenu;
    private final ResponceMenu responceMenu;
    private final ClimatMenu climatMenu;

    public MenuServiceImpl(MainMenu mainMenu,
                           CurrencyMenu currencyMenu,
                           SettingsMenu settingsMenu,
                           HelpMenu helpMenu,
                           AboutMenu aboutMenu,
                           ResponceMenu responceMenu, ClimatMenu climatMenu) {

        this.mainMenu = mainMenu;
        this.currencyMenu = currencyMenu;
        this.settingsMenu = settingsMenu;
        this.helpMenu = helpMenu;
        this.aboutMenu = aboutMenu;
        this.responceMenu = responceMenu;
        this.climatMenu = climatMenu;
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

    public SendMessage createClimateMenu(long chatId) {
        return climatMenu.createMenu(chatId);
    }

    public SendMessage createCurrencyMenu(long chatId) {
        return currencyMenu.createMenu(chatId);
    }

    public SendMessage createCurrencyResultMenu(long chatId, String currencyInfo) {
        return currencyMenu.createResultMenu(chatId, currencyInfo);
    }

    public SendMessage createArduinoAnswerMenu(long chatId, String answer) {
        return responceMenu.createResponceMenu(chatId, answer);
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
