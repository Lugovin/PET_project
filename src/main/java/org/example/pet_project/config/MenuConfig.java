package org.example.pet_project.config;


import org.springframework.stereotype.Component;

/**
 * Конфигурация меню - константы и настройки
 */
@Component
public class MenuConfig {

    // Константы для callback данных
    public static final String CB_MAIN_MENU = "MAIN_MENU";
    public static final String CB_CURRENCY_MENU = "CURRENCY_MENU";
    public static final String CB_CLIMAT_MENU = "CLIMAT_MENU";
    public static final String CB_CURRENCY_SELECT = "CURR_";
    public static final String CB_SETTINGS = "SETTINGS";
    public static final String CB_HELP = "HELP";
    public static final String CB_ABOUT = "ABOUT";
    public static final String CB_ALL_CURRENCIES = "ALL_CURR";
    public static final String CB_BACK = "BACK";


    // Тексты сообщений
    public static class Texts {
        public static String getWelcomeText(String userName) {
            return String.format(
                    "👋 Привет, %s!\n\n" +
                    "Я бот для контроля и управления за умным домом.\n" +
                    "Используйте меню ниже для навигации.",
                    userName
            );
        }

        public static final String MAIN_MENU_TITLE = "🏦 *Главное меню*";
        public static final String CLIMAT_MENU_TITLE = "🏦 *Меню контроля климата*";
        public static final String CURRENCY_MENU_TITLE = "💱 *Выбор валюты*\nВведите код валюты или выберите из списка:";
        public static final String SETTINGS_MENU_TITLE = "⚙️ *Настройки*\n\nЗдесь вы можете настроить параметры бота.\nФункционал настроек будет добавлен в будущих обновлениях.";
        public static final String HELP_MENU_TITLE = "❓ *Помощь*\n\n*ТУТ ПОКА ПУСТО*\n";
        public static final String ABOUT_MENU_TITLE = "ℹ️ *О боте*\n\n*Бот для контроля и управления умным домом*\n\n✅ Может получать актуальные курсы ЦБ РФ\nАвтор - Луговин Николай.";
        public static final String ALL_CURRENCIES_TITLE = "📋 *Список доступных валют:*\n\n%s\n\nИспользуйте код валюты для получения курса.";
    }
}
