package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class ResponceMenu extends BaseMenu {
    protected ResponceMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {

        return null;
    }

    public SendMessage createResponceMenu(long chatId, String responce) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .button("🔙 Назад", MenuConfig.CB_BACK)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(responce)
                .inlineKeyboard(keyboard)
                .build();
    }
}
