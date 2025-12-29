package org.example.pet_project.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для управления сессиями пользователей
 */
@Service
public class UserSessionService {

    public enum UserState {
        MAIN_MENU,
        CLIMAT_MENU,
        CURRENCY_SELECTION,
        VIEWING_CURRENCY,
        SETTINGS,
        HELP,
        ALL_CURRENCIES,
        ARDUINO_ANSWER
    }

    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, Integer> userMenuMessages = new HashMap<>();
    private final Map<Long, String> userSelectedCurrency = new HashMap<>();

    public UserState getUserState(long chatId) {
        return userStates.getOrDefault(chatId, UserState.MAIN_MENU);
    }

    public void setUserState(long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public void saveMenuMessageId(long chatId, int messageId) {
        userMenuMessages.put(chatId, messageId);
    }

    public Integer getMenuMessageId(long chatId) {
        return userMenuMessages.get(chatId);
    }

    public void removeMenuMessageId(long chatId) {
        userMenuMessages.remove(chatId);
    }

    public void saveSelectedCurrency(long chatId, String currencyCode) {
        userSelectedCurrency.put(chatId, currencyCode);
    }

    public String getSelectedCurrency(long chatId) {
        return userSelectedCurrency.get(chatId);
    }

    public void clearUserSession(long chatId) {
        userStates.remove(chatId);
        userMenuMessages.remove(chatId);
        userSelectedCurrency.remove(chatId);
    }
}
