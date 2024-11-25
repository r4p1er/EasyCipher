package ru.obninsk.iate.easycipher;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class Main {
    private final static int INITIAL_WIDTH = 600;
    private final static int INITIAL_HEIGHT = 600;

    public static void main(String[] args) {
        maybeLoadFonts();
        SwingUtilities.invokeLater(() -> {
            var mainWindow = new MainWindow();
            mainWindow.setBounds(0, 0, INITIAL_WIDTH, INITIAL_HEIGHT);
            mainWindow.setLocationRelativeTo(null);
            mainWindow.setVisible(true);
        });
    }

    private static void maybeLoadFonts() {
        InputStream monospaceFontStream =
                Main.class.getResourceAsStream("/fonts/jetbrains-mono-font.ttf");

        try {
            if (monospaceFontStream == null) throw new Exception();
            var monospaceFont = Font.createFont(Font.TRUETYPE_FONT, monospaceFontStream);
            UIManager.put("Label.font", monospaceFont.deriveFont(Font.PLAIN, 14));
            UIManager.put("TextPane.font", monospaceFont.deriveFont(Font.PLAIN, 14));
            UIManager.put("Button.font", monospaceFont.deriveFont(Font.PLAIN, 14));
        } catch (Exception e) {
            System.out.println("Error occurred while loading fonts");
        }
    }
}
