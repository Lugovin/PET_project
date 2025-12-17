package org.example.pet_project.menu;


import org.example.pet_project.builder.MessageBuilder;
import org.example.pet_project.config.MenuConfig;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Базовый класс для всех меню
 */
public abstract class BaseMenu {

    protected final MenuConfig config;

    protected BaseMenu(MenuConfig config) {
        this.config = config;
    }

    /**
     * Создает сообщение с меню
     */
    public abstract SendMessage createMenu(long chatId);

    /**
     * Вспомогательный метод для создания базового сообщения
     */
    protected SendMessage createBaseMessage(long chatId, String text) {
        return MessageBuilder.create(chatId)
                .text(text)
                .build();
    }
}
