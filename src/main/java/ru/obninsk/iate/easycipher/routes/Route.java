package ru.obninsk.iate.easycipher.routes;

import javax.swing.*;

public abstract class Route {
    private final String title;

    public Route(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract JPanel getContentPane();
}
