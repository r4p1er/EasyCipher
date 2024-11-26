package ru.obninsk.iate.easycipher;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.routes.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private static MainFrame instance;

    private MainFrame() {
        navigate(new StartRoute());
    }

    public static synchronized MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    public void navigate(@NotNull Route destinationRoute) {
        setTitle(destinationRoute.getName());
        setContentPane(destinationRoute.getContentPane());
        revalidate();
    }
}
