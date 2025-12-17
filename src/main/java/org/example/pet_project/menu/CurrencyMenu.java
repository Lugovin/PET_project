package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Меню выбора валюты
 */
@Component
public class CurrencyMenu extends BaseMenu {

    public CurrencyMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🇺🇸 USD", MenuConfig.CB_CURRENCY_SELECT + "USD")
                .button("🇪🇺 EUR", MenuConfig.CB_CURRENCY_SELECT + "EUR")
                .button("🇬🇧 GBP", MenuConfig.CB_CURRENCY_SELECT + "GBP")
                .endRow()
                .row()
                .button("🇯🇵 JPY", MenuConfig.CB_CURRENCY_SELECT + "JPY")
                .button("🇨🇳 CNY", MenuConfig.CB_CURRENCY_SELECT + "CNY")
                .button("🇨🇭 CHF", MenuConfig.CB_CURRENCY_SELECT + "CHF")
                .endRow()
                .row()
                .button("🇨🇦 CAD", MenuConfig.CB_CURRENCY_SELECT + "CAD")
                .button("🇦🇺 AUD", MenuConfig.CB_CURRENCY_SELECT + "AUD")
                .button("🇷🇺 RUB", MenuConfig.CB_CURRENCY_SELECT + "RUB")
                .endRow()
                .row()
                .button("📋 Все валюты", MenuConfig.CB_ALL_CURRENCIES)
                .button("🔙 Назад", MenuConfig.CB_BACK)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.CURRENCY_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }

    /**
     * Создает меню с результатом выбора валюты
     */
    public SendMessage createResultMenu(long chatId, String currencyInfo) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("📊 Еще валюты", MenuConfig.CB_CURRENCY_MENU)
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(currencyInfo)
                .inlineKeyboard(keyboard)
                .build();
    }
}
