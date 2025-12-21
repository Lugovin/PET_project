package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class ArduinoAnswerMenu extends BaseMenu {
    protected ArduinoAnswerMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {

        return null;
    }

    public SendMessage createArduinoResponceMenu(long chatId, String responce) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(responce)
                .inlineKeyboard(keyboard)
                .build();
    }
}
