package org.example.pet_project.menu;


import org.example.pet_project.builder.InlineKeyboardBuilder;
import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * Главное меню бота
 */
@Component
public class MainMenu extends BaseMenu {

    public MainMenu(MenuConfig config) {
        super(config);
    }

    @Override
    public SendMessage createMenu(long chatId) {
        InlineKeyboardBuilder builder = InlineKeyboardBuilder.create();

        // Первые два ряда
        builder.row()
                .button("💱 Курсы валют", MenuConfig.CB_CURRENCY_MENU)
                .button("⚙️ Настройки", MenuConfig.CB_SETTINGS)
                .endRow()
                .row()
                .button("ВКЛ", "RELAY:0:ON")
                .button("ВЫКЛ", "RELAY:0:OFF")
                .endRow()
                .row()
                .button("GET ALL", "GETALL")
                .button("STATUS", "STATUS")
                .endRow()
                .endRow()
                .row()
                .button("Connect", "Connect")
                .button("Disconnect", "Disconnect")
                .endRow()
                .row()
                .button("❓ Помощь", MenuConfig.CB_HELP)
                .button("ℹ️ О боте", MenuConfig.CB_ABOUT)
                .endRow();



        InlineKeyboardMarkup keyboard = builder.build();

        return MessageBuilder.create(chatId)
                .text(MenuConfig.Texts.MAIN_MENU_TITLE)
                .inlineKeyboard(keyboard)
                .build();
    }

}
