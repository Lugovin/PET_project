package org.example.pet_project;

import lombok.AllArgsConstructor;
import org.example.pet_project.config.BotProperties;

import org.example.pet_project.services.ValuteService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
//    public final BotProperties botProperties;
//    private final ValuteService valuteService;
//
//    @Override
//    public String getBotUsername() {
//        return botProperties.getName();
//    }
//
//    @Override
//    public String getBotToken() {
//        return botProperties.getToken();
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        String currency = "";
//
//        if(update.hasMessage() && update.getMessage().hasText()){
//            String messageText = update.getMessage().getText();
//            long chatId = update.getMessage().getChatId();
//
//            switch (messageText){
//                case "/start":
//                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
//                    break;
//                case "/valute":
//                    valuteCommandReceived(chatId);
//                    break;
//                default:
//                    try {
//                        currency = valuteService.getValuteRateByCode(messageText.toUpperCase());
//                    } catch (IOException e) {
//                        sendMessage(chatId, "Такого кода валюты не существует.");
//                    }
//                    sendMessage(chatId, currency);
//            }
//        }
//
//    }
//
//    private void startCommandReceived(Long chatId, String name) {
//        String answer = "Привет, " + name + ", рад новой встрече!";
//        sendMessage(chatId, answer);
//    }
//
//    private void valuteCommandReceived(Long chatId) {
//        String answer = "Введи код валюты, курс которой ты хотел бы узнать" + "\n" +
//                        "Например: USD";
//        sendMessage(chatId, answer);
//    }
//
//    private void sendMessage(Long chatId, String textToSend){
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(String.valueOf(chatId));
//        sendMessage.setText(textToSend);
//        try {
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//
//        }
//    }




    public final BotProperties botProperties;
    private final ValuteService valuteService;

    // Хранилище состояний пользователей
    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, Integer> userMenuMessages = new HashMap<>();
    private final Map<Long, String> userSelectedCurrency = new HashMap<>();

    // Константы для callback данных
    private static final String CB_MAIN_MENU = "MAIN_MENU";
    private static final String CB_CURRENCY_MENU = "CURRENCY_MENU";
    private static final String CB_CURRENCY_SELECT = "CURR_";
    private static final String CB_SETTINGS = "SETTINGS";
    private static final String CB_HELP = "HELP";
    private static final String CB_ABOUT = "ABOUT";
    private static final String CB_ALL_CURRENCIES = "ALL_CURR";
    private static final String CB_BACK = "BACK";

    // Основные валюты для быстрого доступа
    private static final List<String> MAIN_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "CNY", "CHF", "CAD", "AUD"
    );

    // Состояния пользователя
    private enum UserState {
        MAIN_MENU,
        CURRENCY_SELECTION,
        VIEWING_CURRENCY,
        SETTINGS,
        HELP
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
        if (update.hasCallbackQuery()) {
            // Обработка нажатий на inline-кнопки
            handleCallbackQuery(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            // Обработка текстовых сообщений
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();

        // Удаляем предыдущее меню, если оно было
        clearPreviousMenu(chatId);

        switch (messageText) {
            case "/start":
                userStates.put(chatId, UserState.MAIN_MENU);
                startCommandReceived(chatId, message.getChat().getFirstName());
                break;

            case "/menu":
                userStates.put(chatId, UserState.MAIN_MENU);
                showMainMenu(chatId);
                break;

            case "/valute":
            case "Валюты":
                userStates.put(chatId, UserState.CURRENCY_SELECTION);
                showCurrencyMenu(chatId);
                break;

            case "/help":
            case "Помощь":
                userStates.put(chatId, UserState.HELP);
                showHelpMenu(chatId);
                break;

            case "/settings":
            case "Настройки":
                userStates.put(chatId, UserState.SETTINGS);
                showSettingsMenu(chatId);
                break;

            case "/back":
            case "Назад":
                navigateBack(chatId);
                break;

            default:
                handleUserInput(chatId, messageText);
        }
    }

    //  Ручной ввод кода валюты
    private void handleUserInput(long chatId, String input) {
        UserState currentState = userStates.getOrDefault(chatId, UserState.MAIN_MENU);

        switch (currentState) {
            case CURRENCY_SELECTION:
                // Проверяем, является ли ввод кодом валюты (3 буквы)
                if (input.matches("[A-Za-z]{3}")) {
                    showCurrencyRate(chatId, input.toUpperCase());
                } else {
                    sendMessage(chatId, "Пожалуйста, введите код валюты из 3 букв (например: USD, EUR) или используйте меню.");
                    showCurrencyMenu(chatId);
                }
                break;

            case MAIN_MENU:
                sendMessage(chatId, "Используйте меню для навигации или введите /menu");
                break;

            default:
                sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        // Удаляем предыдущее меню
        clearPreviousMenu(chatId);

        // Обрабатываем callback данные
        if (callbackData.startsWith(CB_CURRENCY_SELECT)) {
            String currencyCode = callbackData.substring(CB_CURRENCY_SELECT.length());
            userSelectedCurrency.put(chatId, currencyCode);
            showCurrencyRate(chatId, currencyCode);
        } else {
            switch (callbackData) {
                case CB_MAIN_MENU:
                    userStates.put(chatId, UserState.MAIN_MENU);
                    showMainMenu(chatId);
                    break;

                case CB_CURRENCY_MENU:
                    userStates.put(chatId, UserState.CURRENCY_SELECTION);
                    showCurrencyMenu(chatId);
                    break;

                case CB_ALL_CURRENCIES:
                    showAllCurrencies(chatId);
                    break;

                case CB_SETTINGS:
                    userStates.put(chatId, UserState.SETTINGS);
                    showSettingsMenu(chatId);
                    break;

                case CB_HELP:
                    userStates.put(chatId, UserState.HELP);
                    showHelpMenu(chatId);
                    break;

                case CB_ABOUT:
                    showAbout(chatId);
                    break;

                case CB_BACK:
                    navigateBack(chatId);
                    break;
            }
        }

        // Отправляем ответ на callback (убирает "часики" у кнопки)
        sendAnswerCallbackQuery(callbackQuery.getId());
    }

    private void navigateBack(long chatId) {
        UserState currentState = userStates.get(chatId);

        switch (currentState) {
            case CURRENCY_SELECTION:
            case VIEWING_CURRENCY:
            case SETTINGS:
            case HELP:
                userStates.put(chatId, UserState.MAIN_MENU);
                showMainMenu(chatId);
                break;
            default:
                userStates.put(chatId, UserState.MAIN_MENU);
                showMainMenu(chatId);
        }
    }

    private void showMainMenu(long chatId) {
        userStates.put(chatId, UserState.MAIN_MENU);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🏦 *Главное меню*\nВыберите действие:");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Первый ряд
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("💱 Курсы валют", CB_CURRENCY_MENU));
        row1.add(createButton("⚙️ Настройки", CB_SETTINGS));

        // Второй ряд
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("❓ Помощь", CB_HELP));
        row2.add(createButton("ℹ️ О боте", CB_ABOUT));

        // Третий ряд - популярные валюты быстрого доступа
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🇺🇸 USD", CB_CURRENCY_SELECT + "USD"));
        row3.add(createButton("🇪🇺 EUR", CB_CURRENCY_SELECT + "EUR"));
        row3.add(createButton("🇬🇧 GBP", CB_CURRENCY_SELECT + "GBP"));

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMenuMessage(chatId, message);
    }

    private void showCurrencyMenu(long chatId) {
        userStates.put(chatId, UserState.CURRENCY_SELECTION);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("💱 *Выбор валюты*\nВведите код валюты или выберите из списка:");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Первый ряд - основные валюты
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🇺🇸 USD", CB_CURRENCY_SELECT + "USD"));
        row1.add(createButton("🇪🇺 EUR", CB_CURRENCY_SELECT + "EUR"));
        row1.add(createButton("🇬🇧 GBP", CB_CURRENCY_SELECT + "GBP"));

        // Второй ряд
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🇯🇵 JPY", CB_CURRENCY_SELECT + "JPY"));
        row2.add(createButton("🇨🇳 CNY", CB_CURRENCY_SELECT + "CNY"));
        row2.add(createButton("🇨🇭 CHF", CB_CURRENCY_SELECT + "CHF"));

        // Третий ряд
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🇨🇦 CAD", CB_CURRENCY_SELECT + "CAD"));
        row3.add(createButton("🇦🇺 AUD", CB_CURRENCY_SELECT + "AUD"));
        row3.add(createButton("🇷🇺 RUB", CB_CURRENCY_SELECT + "RUB"));

        // Четвертый ряд - дополнительные опции
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("📋 Все валюты", CB_ALL_CURRENCIES));
        row4.add(createButton("🔙 Назад", CB_BACK));

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMenuMessage(chatId, message);
    }

    private void showCurrencyRate(long chatId, String currencyCode) {
        userStates.put(chatId, UserState.VIEWING_CURRENCY);

        try {
            String rateInfo = valuteService.getValuteRateByCode(currencyCode);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(rateInfo);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createButton("📊 Еще валюты", CB_CURRENCY_MENU));
            row1.add(createButton("🏠 Главная", CB_MAIN_MENU));

            rows.add(row1);
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);

            sendMessage(chatId, message);

        } catch (IOException e) {
            sendMessage(chatId, "❌ Валюты с кодом " + currencyCode + " не найдено.\nПопробуйте другой код.");
            showCurrencyMenu(chatId);
        }
    }

    private void showAllCurrencies(long chatId) {
        try {
            // Получаем все доступные валюты из сервиса
            // Предполагаем, что у вас есть метод для получения списка всех валют
            Map<String, String> allCurrencies = valuteService.getAllCurrencies();

            StringBuilder messageText = new StringBuilder("📋 *Список доступных валют:*\n\n");

            int count = 1;
            for (Map.Entry<String, String> entry : allCurrencies.entrySet()) {
                messageText.append(count).append(". ").append(entry.getKey())
                        .append(" - ").append(entry.getValue()).append("\n");
                count++;
            }

            messageText.append("\nИспользуйте код валюты для получения курса.");

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(messageText.toString());
            message.setParseMode("Markdown");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createButton("🔍 Выбрать валюту", CB_CURRENCY_MENU));
            row1.add(createButton("🔙 Назад", CB_BACK));

            rows.add(row1);
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);

            sendMessage(chatId, message);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Не удалось загрузить список валют. Попробуйте позже.");
            showCurrencyMenu(chatId);
        }
    }

    private void showSettingsMenu(long chatId) {
        userStates.put(chatId, UserState.SETTINGS);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("⚙️ *Настройки*\n\n" +
                        "Здесь вы можете настроить параметры бота.\n" +
                        "Функционал настроек будет добавлен в будущих обновлениях.");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🔔 Уведомления", "SETTINGS_NOTIFICATIONS"));
        row1.add(createButton("🎨 Тема", "SETTINGS_THEME"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🔙 Назад", CB_BACK));
        row2.add(createButton("🏠 Главная", CB_MAIN_MENU));

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMenuMessage(chatId, message);
    }

    private void showHelpMenu(long chatId) {
        userStates.put(chatId, UserState.HELP);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("❓ *Помощь*\n\n" +
                        "*Основные команды:*\n" +
                        "`/start` - Начать работу с ботом\n" +
                        "`/menu` - Показать главное меню\n" +
                        "`/valute` - Показать курсы валют\n" +
                        "`/help` - Показать это сообщение\n\n" +
                        "*Как использовать:*\n" +
                        "1. Выберите валюту из меню\n" +
                        "2. Или введите код валюты (например: USD, EUR)\n" +
                        "3. Получите актуальный курс ЦБ РФ\n\n" +
                        "*Примеры кодов валют:*\n" +
                        "USD - Доллар США\n" +
                        "EUR - Евро\n" +
                        "GBP - Фунт стерлингов\n" +
                        "CNY - Китайский юань");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("💱 К валютному меню", CB_CURRENCY_MENU));
        row1.add(createButton("🔙 Назад", CB_BACK));

        rows.add(row1);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMenuMessage(chatId, message);
    }

    private void showAbout(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("ℹ️ *О боте*\n\n" +
                        "*Бот для отслеживания курсов валют*\n\n" +
                        "✅ Актуальные курсы ЦБ РФ\n" +
                        "✅ Удобное меню навигации\n" +
                        "✅ Быстрый доступ к основным валютам\n" +
                        "✅ История изменений курсов\n\n" +
                        "Данные обновляются ежедневно.\n" +
                        "Источник: Центральный банк РФ");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🏠 Главная", CB_MAIN_MENU));
        row1.add(createButton("💱 К валютам", CB_CURRENCY_MENU));

        rows.add(row1);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMessage(chatId, message);
    }

    private void startCommandReceived(Long chatId, String name) {
        String welcomeText = String.format(
                "👋 Привет, %s!\n\n" +
                "Я бот для отслеживания курсов валют ЦБ РФ.\n" +
                "Используйте меню ниже для навигации.",
                name
        );

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);

        // Клавиатура быстрого доступа (ReplyKeyboard)
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("💱 Валюты");
        row1.add("⚙️ Настройки");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("❓ Помощь");
        row2.add("🏠 Главная");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        sendMessage(chatId, message);

        // Через секунду показываем основное меню
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                showMainMenu(chatId);
            }
        }, 1000);
    }

    // Вспомогательные методы

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void sendMenuMessage(long chatId, SendMessage message) {
        try {
            Message sentMessage = execute(message);
            // Сохраняем ID сообщения с меню для возможности его удаления
            userMenuMessages.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void clearPreviousMenu(long chatId) {
        if (userMenuMessages.containsKey(chatId)) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(userMenuMessages.get(chatId));
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                // Игнорируем ошибку, если сообщение уже удалено
            }
            userMenuMessages.remove(chatId);
        }
    }

    private void sendAnswerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}