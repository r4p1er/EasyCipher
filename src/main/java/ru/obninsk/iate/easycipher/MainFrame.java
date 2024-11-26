package ru.obninsk.iate.easycipher;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        var startRoute = new StartRoute();
        setContentPane(startRoute.getContentPane());
        setTitle("EasyCipher");
    }
}
