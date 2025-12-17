package org.example.pet_project.builder;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder для создания InlineKeyboard (кнопки под сообщением)
 */
public class InlineKeyboardBuilder {
    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    private List<InlineKeyboardButton> currentRow;

    private InlineKeyboardBuilder() {}

    public static InlineKeyboardBuilder create() {
        return new InlineKeyboardBuilder();
    }

    public InlineKeyboardBuilder row() {
        if (currentRow != null && !currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }
        currentRow = new ArrayList<>();
        return this;
    }

    public InlineKeyboardBuilder button(String text, String callbackData) {
        if (currentRow == null) {
            row();
        }

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        currentRow.add(button);

        return this;
    }

    public InlineKeyboardBuilder button(String text, String callbackData, String url) {
        if (currentRow == null) {
            row();
        }

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        button.setUrl(url);
        currentRow.add(button);

        return this;
    }

    public InlineKeyboardBuilder buttons(String[][] buttonConfigs) {
        for (String[] config : buttonConfigs) {
            if (config.length == 2) {
                button(config[0], config[1]);
            } else if (config.length == 3) {
                button(config[0], config[1], config[2]);
            }
        }
        return this;
    }

    public InlineKeyboardBuilder endRow() {
        if (currentRow != null && !currentRow.isEmpty()) {
            keyboard.add(currentRow);
            currentRow = null;
        }
        return this;
    }

    public InlineKeyboardMarkup build() {
        if (currentRow != null && !currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        return markup;
    }
}