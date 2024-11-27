package ru.obninsk.iate.easycipher.routes;

import ru.obninsk.iate.easycipher.*;
import javax.swing.*;
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

    public StartRoute() {
        super("EasyCipher");
        chooserPanelButton.addActionListener(this::handleChooserPanelButtonAction);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
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
