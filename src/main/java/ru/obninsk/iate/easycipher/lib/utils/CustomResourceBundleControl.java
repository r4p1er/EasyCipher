package ru.obninsk.iate.easycipher.lib.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class CustomResourceBundleControl extends ResourceBundle.Control {

    private final String resourceFolder;

    public CustomResourceBundleControl(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload) throws IOException {

        String language = locale.getLanguage();
        String resourceName = resourceFolder + "/" + baseName + "_" + language + ".properties";

        InputStream stream = loader.getResourceAsStream(resourceName);
        if (stream == null) {
            resourceName = resourceFolder + "/" + baseName + ".properties";
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream != null) {
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            }
        }

        return null;
    }
}
