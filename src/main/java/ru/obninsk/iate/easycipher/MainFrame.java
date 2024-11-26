package ru.obninsk.iate.easycipher;

import javax.swing.*;

public class MainFrame extends JFrame {
    private Route currentRoute;

    public MainFrame() {
        navigate(new StartRoute());
    }

    public void navigate(Route currentRoute) {
        this.currentRoute = currentRoute;
        setTitle(currentRoute.getName());
        setContentPane(currentRoute.getContentPane());
    }
}
