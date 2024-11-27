package ru.obninsk.iate.easycipher.routes;

import ru.obninsk.iate.easycipher.*;
import ru.obninsk.iate.easycipher.components.RecentItemButton;

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

    public StartRoute() {
        super("EasyCipher");
        renderRecentItemsPanel();
        chooserPanelButton.addActionListener(this::handleChooserPanelButtonAction);
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
        recentItemsPanel.setMinimumSize(new Dimension(440, -1));
        recentItemsPanel.setMaximumSize(new Dimension(440, -1));
    }

    private void renderRecentItemsPanel() {
        var recentItems = MainFrame.getInstance().getRecentlyEncryptedItems();

        for (File item : recentItems) {
            recentItemsPanel.add(new RecentItemButton(item));
        }
        recentItemsPanel.revalidate();
    }

    private void handleChooserPanelButtonAction(ActionEvent event) {
        var fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle("Choose a file or directory to encrypt");
        fileChooser.setApproveButtonText("Choose");
        int result = fileChooser.showOpenDialog(contentPane);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedItem = fileChooser.getSelectedFile();
        var encryptRoute = new EncryptRoute(selectedItem);
        MainFrame.getInstance().navigate(encryptRoute);
    }
}
