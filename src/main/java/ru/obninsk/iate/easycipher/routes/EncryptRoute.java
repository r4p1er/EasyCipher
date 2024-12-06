package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.enums.Algorithm;
import ru.obninsk.iate.easycipher.lib.utils.LocalizationUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptRoute extends Route {
    private JPanel contentPane;
    private JScrollPane contentScrollPane;
    private JPanel contentPaneInner;
    private JPanel openedItemPanel;
    private JPanel openedItemPanelInner;
    private JPanel algorithmPanel;
    private JLabel algorithmPanelLabel;
    private JComboBox<String> algorithmPanelComboBox;
    private JTextPane algorithmPanelDescription;
    private JPanel keygenPanel;
    private JLabel keygenPanelLabel;
    private JTextField keygenPanelTextField;
    private JButton keygenPanelGenerateButton;
    private JButton encryptButton;
    private JButton cancelButton;

    private final File targetItem;
    private Algorithm selectedAlgorithm = Algorithm.AES;
    private static final String[] ALGORITHM_OPTIONS = { "AES", "Blowfish", "Twofish" };
    private static final String[] ALGORITHM_DESCRIPTIONS = {
            LocalizationUtility.getLocalizedString("label.aes.description"),
            LocalizationUtility.getLocalizedString("label.blowfish.description"),
            LocalizationUtility.getLocalizedString("label.twofish.description")
    };

    public EncryptRoute(@NotNull File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        openedItemPanelInner.add(new OpenedItemLabel(targetItem));
        algorithmPanelDescription.setText(ALGORITHM_DESCRIPTIONS[0]);
        SwingUtilities.invokeLater(() -> algorithmPanelDescription.revalidate());
        algorithmPanelComboBox.addActionListener(this::handleAlgorithmPanelComboBoxAction);
        keygenPanelGenerateButton.addActionListener(this::handleKeygenPanelGenerateButtonAction);
        encryptButton.addActionListener(this::handleEncryptButtonAction);
        cancelButton.addActionListener(this::handleCancelButtonAction);
    }

    public static String generateSecureKey(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[byteLength];
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    private void createUIComponents() {
        algorithmPanelComboBox = new JComboBox<>(ALGORITHM_OPTIONS);
        openedItemPanelInner = new JPanel();
        openedItemPanelInner.setBackground(null);
    }

    private void handleAlgorithmPanelComboBoxAction(ActionEvent event) {
        int selectedIndex = algorithmPanelComboBox.getSelectedIndex();

        if (selectedIndex == 0) selectedAlgorithm = Algorithm.AES;
        else if (selectedIndex == 1) selectedAlgorithm = Algorithm.BLOWFISH;
        else selectedAlgorithm = Algorithm.TWOFISH;

        algorithmPanelDescription.setText(ALGORITHM_DESCRIPTIONS[selectedIndex]);
        SwingUtilities.invokeLater(() -> algorithmPanelDescription.revalidate());
    }

    private void handleKeygenPanelGenerateButtonAction(ActionEvent event) {
        int keySizeBytes = 32;
        String key = generateSecureKey(keySizeBytes);
        keygenPanelTextField.setText(key);
    }

    private void handleEncryptButtonAction(ActionEvent event) {
        var mainFrame = MainFrame.getInstance();
        mainFrame.showNotification(LocalizationUtility.getLocalizedString("notification.item.encrypted"));
        mainFrame.addToRecentItems(targetItem);
        mainFrame.navigate(new StartRoute());
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}
