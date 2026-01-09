package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;

import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Меню помощи
 */
@Component
public class HelpMenu extends BaseMenu {

    public HelpMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🔙 Назад", MenuConfig.CB_BACK)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.HELP_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }
}