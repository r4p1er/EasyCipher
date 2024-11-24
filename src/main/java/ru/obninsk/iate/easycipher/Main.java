package ru.obninsk.iate.easycipher;

import javax.swing.*;

public class Main {
    private final static int INITIAL_WIDTH = 1000;
    private final static int INITIAL_HEIGHT = 800;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var mainWindow = new MainWindow();
            mainWindow.setBounds(0, 0, INITIAL_WIDTH, INITIAL_HEIGHT);
            mainWindow.setLocationRelativeTo(null);
            mainWindow.setVisible(true);
        });
    }
}
