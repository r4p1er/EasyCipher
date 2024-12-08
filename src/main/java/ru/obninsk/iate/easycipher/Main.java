package ru.obninsk.iate.easycipher;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.InputStream;

public class Main {
    private final static Dimension INITIAL_DIMENSION = new Dimension(610, 610);
    private final static Dimension MINIMUM_DIMENSION = new Dimension(550, 450);

    public static void main(String[] args) {
        FlatOneDarkIJTheme.setup();
        maybeSetDefaultFont();

        SwingUtilities.invokeLater(() -> {
            var mainFrame = MainFrame.getInstance();
            mainFrame.setSize(INITIAL_DIMENSION);
            mainFrame.setMinimumSize(MINIMUM_DIMENSION);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    private static void maybeSetDefaultFont() {
        try (InputStream monospaceFontStream = Main.class.getResourceAsStream(
                "/fonts/jetbrains-mono-font.ttf"
        )) {
            if (monospaceFontStream == null) throw new Exception();
            var monospaceFont = Font.createFont(Font.TRUETYPE_FONT, monospaceFontStream);

            var keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, monospaceFont.deriveFont(Font.PLAIN, 14));
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred while loading the font");
        }
    }
}
