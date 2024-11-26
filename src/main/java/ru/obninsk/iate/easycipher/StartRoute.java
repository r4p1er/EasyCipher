package ru.obninsk.iate.easycipher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class StartRoute {
    private JPanel contentPane;
    private JTextPane titleTextPane;
    private JPanel chooserPanel;
    private JLabel chooserPanelLabel;
    private JButton chooserPanelButton;

    private File selectedFile;

    public StartRoute() {
        chooserPanelButton.addActionListener(this::handleChooserPanelButtonAction);
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private void handleChooserPanelButtonAction(ActionEvent event) {
        var fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(contentPane);
        if (result != JFileChooser.APPROVE_OPTION) return;

        selectedFile = fileChooser.getSelectedFile();
    }
}
