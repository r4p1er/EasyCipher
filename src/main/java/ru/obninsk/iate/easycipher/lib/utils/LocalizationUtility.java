package ru.obninsk.iate.easycipher.lib.utils;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

public class LocalizationUtility {

    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle currentBundle;
    private static final String RESOURCE_FOLDER = "locales";

    public static void setLocale() {
        if (!currentLocale.getCountry().isEmpty()) {
            currentLocale = new Locale(currentLocale.getLanguage());
        }
        currentBundle = ResourceBundle.getBundle(
                "MessagesBundle",
                currentLocale,
                new CustomResourceBundleControl(RESOURCE_FOLDER));

        updateUIManager(currentBundle);
    }

    private static void updateUIManager(ResourceBundle bundle) {
        for (String key : bundle.keySet()) {
            String value = bundle.getString(key);
            UIManager.put(key, value);
        }
    }

    public static String getLocalizedString(String key) {
        return currentBundle.getString(key);
    }
}
