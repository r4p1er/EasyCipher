package ru.obninsk.iate.easycipher;

import javax.swing.*;
import java.awt.event.ActionEvent;

// TODO: Localize / move out all the hardcoded strings in form

public class MainWindow extends JFrame {
    private JPanel contentPane;
    private JTextPane titleTextPane;
    private JPanel chooserPanel;
    private JLabel chooserPanelLabel;
    private JButton chooserPanelButton;

    public MainWindow() {
        setTitle("EasyCipher");
        setContentPane(contentPane);
        chooserPanelButton.addActionListener(this::handleChooserPanelButtonClick);
    }

    private void handleChooserPanelButtonClick(ActionEvent event) {
        System.out.println("handleChooserPanelButtonClick");
    }
}
