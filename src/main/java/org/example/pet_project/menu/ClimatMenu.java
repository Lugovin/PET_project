package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;


@Component
public class ClimatMenu extends BaseMenu {

    public ClimatMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        InlineKeyboardBuilder builder = InlineKeyboardBuilder.create();

        // Первые два ряда
        builder.row()
                .button("Room1", "Room1")
                .button("Room2", "Room2")
                .endRow()
                .row()
                .button("Все датчики.", "Climate")
                .endRow()
                .row()
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .endRow();



        InlineKeyboardMarkup keyboard = builder.build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.CLIMAT_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }
}
