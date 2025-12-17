package org.example.pet_project.menu;

import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;

import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Меню настроек
 */
@Component
public class SettingsMenu extends BaseMenu {

    public SettingsMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        var keyboard = InlineKeyboardBuilder.create()
                .row()
                .button("🔔 Уведомления", "SETTINGS_NOTIFICATIONS")
                .button("🎨 Тема", "SETTINGS_THEME")
                .endRow()
                .row()
                .button("🔙 Назад", MenuConfig.CB_BACK)
                .button("🏠 Главная", MenuConfig.CB_MAIN_MENU)
                .endRow()
                .build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.SETTINGS_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }
}