package ru.obninsk.iate.easycipher.components;

import org.jetbrains.annotations.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class RecentItemButton extends JButton {
    private final File item;

    public RecentItemButton(@NotNull File item) {
        this.item = item;
        render();
    }

    private void render() {
        setMargin(new Insets(10, 20, 10, 20));
        setIconTextGap(12);
        setText(item.getPath());
        String iconAssetPath = item.isDirectory()
                ? "/icons/recent-directory-icon.png"
                : "/icons/recent-file-icon.png";
        try {
            URL iconStream = RecentItemButton.class.getResource(iconAssetPath);
            if (iconStream == null) throw new Exception();

            var icon = new ImageIcon(iconStream);
            setIcon(icon);
        } catch (Exception e) {
            System.err.println("Error occurred while loading '" + iconAssetPath + "' icon");
        }
    }
}
