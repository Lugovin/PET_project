//package org.example.pet_project.builder;
//
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Builder для создания ReplyKeyboard (клавиатура под полем ввода)
// */
//public class KeyboardBuilder {
//    private final List<KeyboardRow> keyboard = new ArrayList<>();
//    private KeyboardRow currentRow;
//    private boolean resizeKeyboard = true;
//    private boolean oneTimeKeyboard = false;
//
//    private KeyboardBuilder() {}
//
//    public static KeyboardBuilder create() {
//        return new KeyboardBuilder();
//    }
//
//    public KeyboardBuilder row() {
//        if (currentRow != null && !currentRow.isEmpty()) {
//            keyboard.add(currentRow);
//        }
//        currentRow = new KeyboardRow();
//        return this;
//    }
//
//    public KeyboardBuilder button(String text) {
//        if (currentRow == null) {
//            row();
//        }
//        currentRow.add(text);
//        return this;
//    }
//
//    public KeyboardBuilder buttons(String... texts) {
//        for (String text : texts) {
//            button(text);
//        }
//        return this;
//    }
//
//    public KeyboardBuilder endRow() {
//        if (currentRow != null && !currentRow.isEmpty()) {
//            keyboard.add(currentRow);
//            currentRow = null;
//        }
//        return this;
//    }
//
//    public KeyboardBuilder resizeKeyboard(boolean resize) {
//        this.resizeKeyboard = resize;
//        return this;
//    }
//
//    public KeyboardBuilder oneTimeKeyboard(boolean oneTime) {
//        this.oneTimeKeyboard = oneTime;
//        return this;
//    }
//
//    public ReplyKeyboardMarkup build() {
//        if (currentRow != null && !currentRow.isEmpty()) {
//            keyboard.add(currentRow);
//        }
//
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        keyboardMarkup.setKeyboard(keyboard);
//        keyboardMarkup.setResizeKeyboard(resizeKeyboard);
//        keyboardMarkup.setOneTimeKeyboard(oneTimeKeyboard);
//
//        return keyboardMarkup;
//    }
//}