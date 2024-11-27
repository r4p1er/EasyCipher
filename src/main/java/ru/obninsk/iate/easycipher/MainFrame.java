package ru.obninsk.iate.easycipher;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.routes.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private final ArrayList<File> recentlyEncryptedItems = new ArrayList<>();

    private MainFrame() {
        SwingUtilities.invokeLater(() -> navigate(new StartRoute()));
    }

    public static synchronized MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    public void navigate(@NotNull Route destinationRoute) {
        setTitle(destinationRoute.getTitle());
        setContentPane(destinationRoute.getContentPane());
        revalidate();
    }

    public ArrayList<File> getRecentlyEncryptedItems() {
        return recentlyEncryptedItems;
    }

    public void addItemToRecentlyEncrypted(File newItem) {
        String newItemPath = newItem.getPath();
        if (recentlyEncryptedItems.stream()
                .anyMatch((item) -> item.getPath().equals(newItemPath))) return;

        recentlyEncryptedItems.add(newItem);
    }
}
