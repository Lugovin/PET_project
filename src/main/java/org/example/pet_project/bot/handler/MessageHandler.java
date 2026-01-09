package org.example.pet_project.bot.handler;



import org.example.pet_project.models.UserState;
import org.example.pet_project.services.MenuService;
import org.example.pet_project.services.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * Обработчик текстовых сообщений
 */
@Component
public class MessageHandler {

    private final MenuService menuService;
    private final UserSessionService userSessionService;

    public MessageHandler(MenuService menuService, UserSessionService userSessionService) {
        this.menuService = menuService;
        this.userSessionService = userSessionService;
    }

    public void handleMessage(Message message) throws TelegramApiException {
        String messageText = message.getText();
        long chatId = message.getChatId();

        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        // Обработка команд
        if (messageText.startsWith("/")) {
            handleCommand(chatId, messageText, message.getChat().getFirstName());
        } else {
            // Обработка обычных сообщений на основе состояния
            handleUserInput(chatId, messageText);
        }
    }

    private void handleCommand(long chatId, String command, String userName) {
        switch (command.toLowerCase()) {
            case "/start":
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
                // Приветственное сообщение будет отправлено из бота
                break;

            case "/menu":
            case "/main":
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
                // Главное меню будет отправлено из бота
                break;

            case "/valute":
            case "/currency":
                userSessionService.setUserState(chatId, UserState.CURRENCY_SELECTION);
                // Меню валют будет отправлено из бота
                break;

            case "/help":
                userSessionService.setUserState(chatId, UserState.HELP);
                // Меню помощи будет отправлено из бота
                break;

            case "/settings":
                userSessionService.setUserState(chatId, UserState.SETTINGS);
                // Меню настроек будет отправлено из бота
                break;

            case "/back":
                navigateBack(chatId);
                break;

            default:
                // Неизвестная команда
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
        }
    }

    private void handleUserInput(long chatId, String input) {
        UserState currentState = userSessionService.getUserState(chatId);

        switch (currentState) {
            case CURRENCY_SELECTION:
                // Проверяем, является ли ввод кодом валюты (3 буквы)
                if (input.matches("[A-Za-z]{3}")) {
                    userSessionService.saveSelectedCurrency(chatId, input.toUpperCase());
                    userSessionService.setUserState(chatId, UserState.VIEWING_CURRENCY);
                    // Курс валюты будет отправлен из бота
                } else {
                    // Сообщение об ошибке будет отправлено из бота
                    userSessionService.setUserState(chatId, UserState.CURRENCY_SELECTION);
                }
                break;

            default:
                // Для других состояний просто переходим в главное меню
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
        }
    }

    private void navigateBack(long chatId) {
        UserState currentState = userSessionService.getUserState(chatId);

        switch (currentState) {
            case CURRENCY_SELECTION:
            case VIEWING_CURRENCY:
            case SETTINGS:
            case HELP:
            case ALL_CURRENCIES:
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
                break;
            default:
                userSessionService.setUserState(chatId, UserState.MAIN_MENU);
        }
    }

    // Геттеры для получения данных для бота
    public UserState getUserState(long chatId) {
        return userSessionService.getUserState(chatId);
    }

    public String getSelectedCurrency(long chatId) {
        return userSessionService.getSelectedCurrency(chatId);
    }
}
