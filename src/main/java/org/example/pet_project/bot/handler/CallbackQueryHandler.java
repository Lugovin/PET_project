package org.example.pet_project.bot.handler;


import jakarta.ws.rs.ext.ParamConverter;
import org.example.pet_project.arduino.ArduinoCommandService;
import org.example.pet_project.bot.TelegramBot;
import org.example.pet_project.config.MenuConfig;
import org.example.pet_project.services.MenuService;
import org.example.pet_project.services.UserSessionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;



/**
 * Обработчик нажатий на inline-кнопки
 */
@Component
public class CallbackQueryHandler {

    private final MenuService menuService;
    private final UserSessionService userSessionService;
    private final ArduinoCommandService arduinoCommandService;
    private final TelegramBot bot;
    String responce = "";

    public CallbackQueryHandler(MenuService menuService, UserSessionService userSessionService, ArduinoCommandService arduinoCommandService, @Lazy TelegramBot bot) {
        this.menuService = menuService;
        this.userSessionService = userSessionService;
        this.arduinoCommandService = arduinoCommandService;
        this.bot = bot;
    }

    public CallbackResult handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        CallbackResult result = new CallbackResult();
        result.setChatId(chatId);

        if (callbackData.startsWith(MenuConfig.CB_CURRENCY_SELECT)) {
            // Выбор валюты
            String currencyCode = callbackData.substring(MenuConfig.CB_CURRENCY_SELECT.length());
            userSessionService.saveSelectedCurrency(chatId, currencyCode);
            userSessionService.setUserState(chatId, UserSessionService.UserState.VIEWING_CURRENCY);
            result.setCurrencyCode(currencyCode);
            result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_RATE); // ← ИСПРАВЛЕНО

        } else {
            // Обработка других callback данных
            switch (callbackData) {
                case MenuConfig.CB_MAIN_MENU:
                    userSessionService.setUserState(chatId, UserSessionService.UserState.MAIN_MENU);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_CURRENCY_MENU:
                    userSessionService.setUserState(chatId, UserSessionService.UserState.CURRENCY_SELECTION);
                    result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_ALL_CURRENCIES:
                    userSessionService.setUserState(chatId, UserSessionService.UserState.ALL_CURRENCIES);
                    result.setAction(CallbackResult.CallbackAction.SHOW_ALL_CURRENCIES); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_SETTINGS:
                    userSessionService.setUserState(chatId, UserSessionService.UserState.SETTINGS);
                    result.setAction(CallbackResult.CallbackAction.SHOW_SETTINGS_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_HELP:
                    userSessionService.setUserState(chatId, UserSessionService.UserState.HELP);
                    result.setAction(CallbackResult.CallbackAction.SHOW_HELP_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_ABOUT:
                    result.setAction(CallbackResult.CallbackAction.SHOW_ABOUT_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_BACK:
                    navigateBack(chatId);
                    UserSessionService.UserState newState = userSessionService.getUserState(chatId);

                    switch (newState) {
                        case MAIN_MENU:
                            result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                            break;
                        case CURRENCY_SELECTION:
                            result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_MENU); // ← ИСПРАВЛЕНО
                            break;
                        default:
                            result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                    }
                    break;
                case "RELAY:0:ON":

                    responce  = arduinoCommandService.setRelay(0, true);
                    System.out.println("Responce from Arduino: " + responce);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU);
                    break;
                case "RELAY:0:OFF":

                    responce  = arduinoCommandService.setRelay(0, false);
                    System.out.println("Responce from Arduino: " + responce);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU);
                    break;
                case "GETALL":

                    responce  = arduinoCommandService.readAllSensors();
                    System.out.println("Responce from Arduino: " + responce);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU);
                    break;
                case "STATUS":

                    responce  = arduinoCommandService.getStatus();
                    System.out.println("Responce from Arduino: " + responce);

                    // 3. Отправка в Telegram
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("📟 Статус устройства:\n" + responce);
                    bot.send(message);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU);


                    break;
            }
        }

        return result;
    }

    private void navigateBack(long chatId) {
        UserSessionService.UserState currentState = userSessionService.getUserState(chatId);

        switch (currentState) {
            case CURRENCY_SELECTION:
            case VIEWING_CURRENCY:
            case SETTINGS:
            case HELP:
            case ALL_CURRENCIES:
                userSessionService.setUserState(chatId, UserSessionService.UserState.MAIN_MENU);
                break;
            default:
                userSessionService.setUserState(chatId, UserSessionService.UserState.MAIN_MENU);
        }
    }

    // Вспомогательный класс для возврата результата обработки
    public static class CallbackResult {
        private long chatId;
        private CallbackAction action;
        private String currencyCode;

        // Вынесенный enum для действий
        public enum CallbackAction {
            SHOW_MAIN_MENU,
            SHOW_CURRENCY_MENU,
            SHOW_CURRENCY_RATE,
            SHOW_SETTINGS_MENU,
            SHOW_HELP_MENU,
            SHOW_ABOUT_MENU,
            SHOW_ALL_CURRENCIES,
            DELETE_PREVIOUS_MENU,
            SEND_ARDUINO_RESPONSE
        }

        // Геттеры и сеттеры
        public long getChatId() {
            return chatId;
        }

        public void setChatId(long chatId) {
            this.chatId = chatId;
        }

        public CallbackAction getAction() {
            return action;
        }

        public void setAction(CallbackAction action) {
            this.action = action;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
    }
}
