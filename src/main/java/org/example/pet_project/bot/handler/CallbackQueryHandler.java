package org.example.pet_project.bot.handler;



import org.example.pet_project.config.MenuConfig;
import org.example.pet_project.models.UserState;
import org.example.pet_project.services.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


/**
 * Обработчик нажатий на inline-кнопки
 */
@Component
public class CallbackQueryHandler {

    private final UserSessionService userSessionService;

    public CallbackQueryHandler(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }


    //========================================================================================
//    Получает данные от нажатой кнопки (callbackData)
//    Определяет, какая кнопка была нажата
//    Меняет состояние пользователя
//    Выполняет нужное действие (меню, курс валют, команда Arduino)
//    Возвращает CallbackResult, который говорит боту, что показывать дальше


    public CallbackResult handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        CallbackResult result = new CallbackResult();
        result.setChatId(chatId);

        if (callbackData.startsWith(MenuConfig.CB_CURRENCY_SELECT)) {
            // Выбор валюты
            String currencyCode = callbackData.substring(MenuConfig.CB_CURRENCY_SELECT.length());
            userSessionService.saveSelectedCurrency(chatId, currencyCode);
            userSessionService.setUserState(chatId, UserState.VIEWING_CURRENCY);
            result.setCurrencyCode(currencyCode);
            result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_RATE); // ← ИСПРАВЛЕНО

        } else {
            // Обработка других callback данных
            switch (callbackData) {
                case MenuConfig.CB_MAIN_MENU:
                    userSessionService.setUserState(chatId, UserState.MAIN_MENU);
                    result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_CURRENCY_MENU:
                    userSessionService.setUserState(chatId, UserState.CURRENCY_SELECTION);
                    result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_CLIMAT_MENU:
                    userSessionService.setUserState(chatId, UserState.CLIMAT_MENU);
                    result.setAction(CallbackResult.CallbackAction.SHOW_CLIMATE_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_ALL_CURRENCIES:
                    userSessionService.setUserState(chatId, UserState.ALL_CURRENCIES);
                    result.setAction(CallbackResult.CallbackAction.SHOW_ALL_CURRENCIES); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_SETTINGS:
                    userSessionService.setUserState(chatId, UserState.SETTINGS);
                    result.setAction(CallbackResult.CallbackAction.SHOW_SETTINGS_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_HELP:
                    userSessionService.setUserState(chatId, UserState.HELP);
                    result.setAction(CallbackResult.CallbackAction.SHOW_HELP_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_ABOUT:
                    result.setAction(CallbackResult.CallbackAction.SHOW_ABOUT_MENU); // ← ИСПРАВЛЕНО
                    break;

                case MenuConfig.CB_BACK:
                    UserState newState = userSessionService.getUserState(chatId);

                    switch (newState) {
                        case MAIN_MENU:
                            result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                            break;
                        case CURRENCY_SELECTION:
                            result.setAction(CallbackResult.CallbackAction.SHOW_CURRENCY_MENU); // ← ИСПРАВЛЕНО
                            break;
                        case CLIMAT_MENU:
                            result.setAction(CallbackResult.CallbackAction.SHOW_CLIMATE_MENU); // ← ИСПРАВЛЕНО
                            break;
                        default:
                            result.setAction(CallbackResult.CallbackAction.SHOW_MAIN_MENU); // ← ИСПРАВЛЕНО
                    }
                    break;

                case "Climate":
                    result.setAction(CallbackResult.CallbackAction.SHOW_ESP32_CLIMATE);
                    break;

                case "Room1":
                    result.setSensorAction(CallbackResult.CallbackAction.SHOW_ESP32_CLIMATE_ROOM, "room1");
                    break;

                case "Room2":
                    result.setSensorAction(CallbackResult.CallbackAction.SHOW_ESP32_CLIMATE_ROOM, "room2");
                    break;
            }
        }

        return result;
    }



    // Вспомогательный класс для возврата результата обработки
    public static class CallbackResult {
        private long chatId;
        private CallbackAction action;
        private String currencyCode;
        private static String sensorId;




        public static String getSensorId() {
            return sensorId;
        }





        // Вынесенный enum для действий
        public enum CallbackAction {
            SHOW_MAIN_MENU,
            SHOW_CURRENCY_MENU,
            SHOW_CLIMATE_MENU,
            SHOW_CURRENCY_RATE,
            SHOW_SETTINGS_MENU,
            SHOW_HELP_MENU,
            SHOW_ABOUT_MENU,
            SHOW_ALL_CURRENCIES,
            DELETE_PREVIOUS_MENU,
            SHOW_ARDUINO_RESPONSE_STATUS,
            SHOW_ARDUINO_CONNECT_STATUS,
            SHOW_ARDUINO_RESPONSE_RELAY,
            SHOW_ESP32_CLIMATE,
            SHOW_ESP32_CLIMATE_ROOM
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


        public void setSensorAction(CallbackAction action, String sensorId) {
            this.action = action;
            this.sensorId = sensorId;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
    }
}
