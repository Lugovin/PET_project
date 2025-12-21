package org.example.pet_project.bot;



import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.example.pet_project.bot.handler.CallbackQueryHandler;
import org.example.pet_project.bot.handler.MessageHandler;
import org.example.pet_project.services.MenuService;
import org.example.pet_project.services.UserSessionService;
import org.example.pet_project.services.ValuteService;
import org.example.pet_project.config.BotProperties;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final ValuteService valuteService;
    private final MenuService menuService;
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final UserSessionService userSessionService;

    public TelegramBot(BotProperties botProperties,
                       ValuteService valuteService,
                       MenuService menuService,
                       MessageHandler messageHandler,
                       CallbackQueryHandler callbackQueryHandler,
                       UserSessionService userSessionService) {
        this.botProperties = botProperties;
        this.valuteService = valuteService;
        this.menuService = menuService;
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.userSessionService = userSessionService;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        String messageText = message.getText();

        // Удаляем предыдущее меню
        clearPreviousMenu(chatId);

        if (messageText.equals("/start")) {
            handleStartCommand(chatId, message.getChat().getFirstName());
            return;
        }

        // Обрабатываем сообщение через handler
        messageHandler.handleMessage(message);

        // Получаем текущее состояние
        UserSessionService.UserState currentState = messageHandler.getUserState(chatId);

        // Выполняем действие в зависимости от состояния
        switch (currentState) {
            case MAIN_MENU:
                sendMenu(menuService.createMainMenu(chatId), chatId);
                break;

            case CURRENCY_SELECTION:
                if (messageText.matches("[A-Za-z]{3}")) {
                    // Пользователь ввел код валюты
                    String currencyCode = messageText.toUpperCase();
                    showCurrencyRate(chatId, currencyCode);
                } else {
                    sendMenu(menuService.createCurrencyMenu(chatId), chatId);
                }
                break;

            case VIEWING_CURRENCY:
                // Пользователь выбрал валюту через ввод
                String currencyCode = messageHandler.getSelectedCurrency(chatId);
                if (currencyCode != null) {
                    showCurrencyRate(chatId, currencyCode);
                } else {
                    sendMenu(menuService.createCurrencyMenu(chatId), chatId);
                }
                break;

            case SETTINGS:
                sendMenu(menuService.createSettingsMenu(chatId), chatId);
                break;

            case HELP:
                sendMenu(menuService.createHelpMenu(chatId), chatId);
                break;



//            case ALL_CURRENCIES:
//                showAllCurrencies(chatId);
//                break;

            default:
                sendMenu(menuService.createMainMenu(chatId), chatId);
        }
    }

    private void handleStartCommand(long chatId, String userName) throws TelegramApiException {
        // Отправляем приветственное сообщение
        execute(menuService.createWelcomeMessage(chatId, userName));

        // Через 1 секунду показываем главное меню
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    userSessionService.setUserState(chatId, UserSessionService.UserState.MAIN_MENU);
                    sendMenu(menuService.createMainMenu(chatId), chatId);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.objects.CallbackQuery callbackQuery = update.getCallbackQuery();
        long chatId = callbackQuery.getMessage().getChatId();

        // Удаляем предыдущее меню
        clearPreviousMenu(chatId);

        // Обрабатываем callback
        CallbackQueryHandler.CallbackResult result = callbackQueryHandler.handleCallbackQuery(callbackQuery);

        // Выполняем действие на основе результата обработки
        switch (result.getAction()) {
            case SHOW_MAIN_MENU:
                sendMenu(menuService.createMainMenu(chatId), chatId);
                break;

            case SHOW_CURRENCY_MENU:
                sendMenu(menuService.createCurrencyMenu(chatId), chatId);
                break;

            case SHOW_CURRENCY_RATE:
                showCurrencyRate(chatId, result.getCurrencyCode());
                break;

            case SHOW_SETTINGS_MENU:
                sendMenu(menuService.createSettingsMenu(chatId), chatId);
                break;

            case SHOW_HELP_MENU:
                sendMenu(menuService.createHelpMenu(chatId), chatId);
                break;

            case SHOW_ABOUT_MENU:
                execute(menuService.createAboutMenu(chatId));
                break;

//            case SHOW_ALL_CURRENCIES:
//                showAllCurrencies(chatId);
//                break;
        }

        // Отправляем ответ на callback (убирает "часики" у кнопки)
        sendAnswerCallbackQuery(callbackQuery.getId());
    }

    private void showCurrencyRate(long chatId, String currencyCode) {
        try {
            String rateInfo = valuteService.getValuteRateByCode(currencyCode);
            execute(menuService.createCurrencyResultMenu(chatId, rateInfo));
        } catch (IOException e) {
            try {
                execute(menuService.createErrorMessage(
                        chatId,
                        "Валюты с кодом " + currencyCode + " не найдено.\nПопробуйте другой код."
                ));
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }

            // Показываем меню валют после ошибки
            try {
                sendMenu(menuService.createCurrencyMenu(chatId), chatId);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

//    private void showAllCurrencies(long chatId) {
//        try {
//            // Получаем список всех валют из сервиса
//            String allCurrencies = valuteService.getAllCurrenciesNames();
//            sendMenu(menuService.createAllCurrenciesMenu(chatId, allCurrencies), chatId);
//
//        } catch (Exception e) {
//            try {
//                execute(menuService.createErrorMessage(
//                        chatId,
//                        "Не удалось загрузить список валют. Попробуйте позже."
//                ));
//                sendMenu(menuService.createCurrencyMenu(chatId), chatId);
//            } catch (TelegramApiException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }

    private void sendMenu(SendMessage menuMessage, long chatId) throws TelegramApiException {
        Message sentMessage = execute(menuMessage);
        userSessionService.saveMenuMessageId(chatId, sentMessage.getMessageId());
    }

    private void clearPreviousMenu(long chatId) {
        Integer previousMessageId = userSessionService.getMenuMessageId(chatId);
        if (previousMessageId != null) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(previousMessageId);
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                // Игнорируем ошибку, если сообщение уже удалено
            }
            userSessionService.removeMenuMessageId(chatId);
        }
    }

    private void sendAnswerCallbackQuery(String callbackQueryId) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQueryId);
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}