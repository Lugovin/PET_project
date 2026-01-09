package org.example.pet_project.services;

import org.example.pet_project.models.UserState;

/**
 * Сервис для управления сессиями пользователей
 */

public interface UserSessionService {


    UserState getUserState(long chatId);

    void setUserState(long chatId, UserState state);

    void saveMenuMessageId(long chatId, int messageId);

    Integer getMenuMessageId(long chatId);

    void removeMenuMessageId(long chatId);

    void saveSelectedCurrency(long chatId, String currencyCode);

    String getSelectedCurrency(long chatId);

    void clearUserSession(long chatId);
}
