package org.example.pet_project.builder;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * Builder для создания сообщений
 */
public class MessageBuilder {
    private final String chatId;
    private String text;
    private String parseMode = "Markdown";
    private ReplyKeyboardMarkup replyKeyboard;
    private InlineKeyboardMarkup inlineKeyboard;

    private MessageBuilder(String chatId) {
        this.chatId = chatId;
    }

    public static MessageBuilder create(long chatId) {
        return new MessageBuilder(String.valueOf(chatId));
    }

    public static MessageBuilder create(String chatId) {
        return new MessageBuilder(chatId);
    }

    public MessageBuilder text(String text) {
        this.text = text;
        return this;
    }

    public MessageBuilder parseMode(String parseMode) {
        this.parseMode = parseMode;
        return this;
    }

    public MessageBuilder disableWebPagePreview() {
        return this;
    }

    public MessageBuilder replyKeyboard(ReplyKeyboardMarkup keyboard) {
        this.replyKeyboard = keyboard;
        return this;
    }

    public MessageBuilder inlineKeyboard(InlineKeyboardMarkup keyboard) {
        this.inlineKeyboard = keyboard;
        return this;
    }

    public SendMessage build() {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(parseMode);

        if (replyKeyboard != null) {
            message.setReplyMarkup(replyKeyboard);
        } else if (inlineKeyboard != null) {
            message.setReplyMarkup(inlineKeyboard);
        }

        return message;
    }
}
