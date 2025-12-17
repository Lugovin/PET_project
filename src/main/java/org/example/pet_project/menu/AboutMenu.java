package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;

import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Меню "О боте"
 */
@Component
public class AboutMenu extends BaseMenu {

    public AboutMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .button("💱 К валютам", MenuConfig.CB_CURRENCY_MENU)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.ABOUT_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }
}
