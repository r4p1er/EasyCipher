package ru.obninsk.iate.easycipher.routes;

import ru.obninsk.iate.easycipher.*;
import ru.obninsk.iate.easycipher.components.RecentItemButton;
import ru.obninsk.iate.easycipher.lib.utils.LocalizationUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class StartRoute extends Route {
    private JPanel contentPane;
    private JScrollPane contentScrollPane;
    private JPanel contentPaneInner;
    private JTextPane titleTextPane;
    private JPanel chooserPanel;
    private JLabel chooserPanelLabel;
    private JButton chooserPanelButton;
    private JPanel recentItemsPanel;
    private JPanel recentItemsPanelWrapper;
    private JButton recentItemsPanelButton;

    public StartRoute() {
        super("EasyCipher");
        renderRecentItemsPanel();
        chooserPanelButton.addActionListener(this::handleChooserPanelButtonAction);
        recentItemsPanelButton.addActionListener(this::handleRecentItemsPanelButtonAction);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    private void createUIComponents() {
        var layout = new GridLayout();
        layout.setColumns(1);
        layout.setRows(-1);
        layout.setVgap(6);
        recentItemsPanel = new JPanel();
        recentItemsPanel.setLayout(layout);
    }

    private void renderRecentItemsPanel() {
        var recentItems = MainFrame.getInstance().getRecentItems();

        if (recentItems.isEmpty()) {
            var label = new JLabel(
                    LocalizationUtility.getLocalizedString("label.no.recent.items"),
                    SwingConstants.CENTER);
            label.setMinimumSize(new Dimension(-1, 30));
            recentItemsPanel.add(label);
        } else for (File item : recentItems) {
            recentItemsPanel.add(new RecentItemButton(item));
        }
        recentItemsPanel.revalidate();
    }

    private void handleChooserPanelButtonAction(ActionEvent event) {
        var fileChooser = getFileChooser();
        if (fileChooser == null)
            return;

        File selectedItem = fileChooser.getSelectedFile();
        var encryptRoute = new EncryptRoute(selectedItem);
        MainFrame.getInstance().navigate(encryptRoute);
    }

    private void handleRecentItemsPanelButtonAction(ActionEvent event) {
        var fileChooser = getFileChooser();
        if (fileChooser == null)
            return;

        File selectedItem = fileChooser.getSelectedFile();
        var decryptRoute = new DecryptRoute(selectedItem);
        MainFrame.getInstance().navigate(decryptRoute);
    }

    private JFileChooser getFileChooser() {
        var fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle(LocalizationUtility.getLocalizedString("dialog.choose.to.encrypt"));
        fileChooser.setApproveButtonText(LocalizationUtility.getLocalizedString("button.choose"));
        int result = fileChooser.showOpenDialog(contentPane);
        if (result != JFileChooser.APPROVE_OPTION)
            return null;

        return fileChooser;
    }
}
