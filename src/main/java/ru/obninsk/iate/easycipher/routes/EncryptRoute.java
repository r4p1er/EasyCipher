package ru.obninsk.iate.easycipher.routes;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class EncryptRoute extends Route {
    private JPanel contentPane;
    private JPanel openedItemPanel;
    private JLabel openedItemPanelLabel;
    private final File targetItem;

    public EncryptRoute(File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        renderOpenedItemLabelPanel();
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    private void renderOpenedItemLabelPanel() {
        openedItemPanelLabel.setText(targetItem.getName());
        String iconAssetPath = targetItem.isDirectory()
                ? "/icons/opened-directory-icon.png"
                : "/icons/opened-file-icon.png";
        try {
            URL iconStream = EncryptRoute.class.getResource(iconAssetPath);
            if (iconStream == null) throw new Exception();

            var icon = new ImageIcon(iconStream);
            openedItemPanelLabel.setIcon(icon);
        } catch (Exception e) {
            System.out.println("Error occurred while loading icon");
        }
    }
}
