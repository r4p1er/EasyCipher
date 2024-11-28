package ru.obninsk.iate.easycipher.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class OpenedItemLabel extends JLabel {
    private final File item;

    public OpenedItemLabel(@NotNull File item) {
        this.item = item;
        render();
    }

    private void render() {
        setIconTextGap(12);
        setFont(getFont().deriveFont(Font.BOLD, 16));
        setText(item.getName());
        String iconAssetPath = item.isDirectory()
                ? "/icons/opened-directory-icon.png"
                : "/icons/opened-file-icon.png";
        try {
            URL iconStream = OpenedItemLabel.class.getResource(iconAssetPath);
            if (iconStream == null) throw new Exception();

            var icon = new ImageIcon(iconStream);
            setIcon(icon);
        } catch (Exception e) {
            System.err.println("Error occurred while loading '" + iconAssetPath + "' icon");
        }
    }
}
