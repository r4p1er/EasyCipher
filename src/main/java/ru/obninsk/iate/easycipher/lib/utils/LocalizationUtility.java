package ru.obninsk.iate.easycipher.lib.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationUtility {

    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle currentBundle;
    private static final String RESOURCE_FOLDER = "locales";

    static {
        setLocale(currentLocale);
    }

    public static void setLocale() {
        currentLocale = Locale.getDefault();
        if (!currentLocale.getCountry().isEmpty()) {
            currentLocale = new Locale(currentLocale.getLanguage());
        }
        currentBundle = ResourceBundle.getBundle(
                "MessagesBundle",
                currentLocale,
                new CustomResourceBundleControl(RESOURCE_FOLDER));
    }

    public static void setLocale(Locale locale) {
        if (!currentLocale.equals(locale)) {
            if (!locale.getCountry().isEmpty()) {
                locale = new Locale(locale.getLanguage());
            }
            currentLocale = locale;
            currentBundle = ResourceBundle.getBundle(
                    "MessagesBundle",
                    currentLocale,
                    new CustomResourceBundleControl(RESOURCE_FOLDER));
        }
    }

    public static String getLocalizedString(String key) {
        return currentBundle.getString(key);
    }
}
