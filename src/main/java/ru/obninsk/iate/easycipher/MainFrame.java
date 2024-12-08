package ru.obninsk.iate.easycipher;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.components.NotificationPanel;
import ru.obninsk.iate.easycipher.routes.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JPanel mainContainer;
    private JPanel notificationContainer;

    private static MainFrame instance;
    private final ArrayList<File> recentItems = new ArrayList<>();

    private MainFrame() {
        setContentPane(contentPane);
        SwingUtilities.invokeLater(() -> navigate(new StartRoute()));
    }

    public static synchronized MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    public void navigate(@NotNull Route destinationRoute) {
        setTitle(destinationRoute.getTitle());
        mainContainer.removeAll();
        mainContainer.add(destinationRoute.getContentPane());
        mainContainer.revalidate();
        SwingUtilities.invokeLater(() -> notificationContainer.revalidate());
    }

    public void showNotification(@NotNull String message) {
        var notificationPanel = new NotificationPanel(message);
        notificationContainer.removeAll();
        notificationContainer.add(notificationPanel);
    }

    public ArrayList<File> getRecentItems() {
        return recentItems;
    }

    public void addToRecentItems(@NotNull File newItem) {
        String newItemPath = newItem.getPath();
        if (recentItems.stream()
                .anyMatch((item) -> item.getPath().equals(newItemPath))) return;

        recentItems.add(newItem);
    }

    private void createUIComponents() {
        var layout = new GridLayout();
        layout.setColumns(1);
        layout.setRows(-1);
        mainContainer = new JPanel();
        mainContainer.setLayout(layout);
        notificationContainer = new JPanel();
        notificationContainer.setLayout(new BorderLayout());
    }
}
