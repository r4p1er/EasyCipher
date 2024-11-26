package ru.obninsk.iate.easycipher;

import javax.swing.*;

public abstract class Route {
    private final String name;

    public Route(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract JPanel getContentPane();
}
