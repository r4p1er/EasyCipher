package ru.obninsk.iate.easycipher;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class Main {
    private final static Dimension INITIAL_DIMENSION = new Dimension(600, 600);
    private final static Dimension MINIMUM_DIMENSION = new Dimension(550, 450);
    private final static Color BACKGROUND_COLOR = new Color(31, 31,31 );

    public static void main(String[] args) {
        maybeLoadFonts();
        SwingUtilities.invokeLater(() -> {
            var mainFrame = new MainFrame();
            mainFrame.setSize(INITIAL_DIMENSION);
            mainFrame.setMinimumSize(MINIMUM_DIMENSION);
            mainFrame.setBackground(BACKGROUND_COLOR);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
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
