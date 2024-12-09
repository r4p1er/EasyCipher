package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.enums.Algorithm;
import ru.obninsk.iate.easycipher.lib.utils.LocalizationUtility;

import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;
import ru.obninsk.iate.easycipher.lib.enums.EncryptionAlgorithm;
import ru.obninsk.iate.easycipher.lib.services.BlowfishCryptoService;
import ru.obninsk.iate.easycipher.lib.services.UserInputHandler;
import ru.obninsk.iate.easycipher.lib.services.AesCryptoService;
import ru.obninsk.iate.easycipher.lib.services.TwofishCryptoService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
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
    private JPanel destinationPanel;
    private JLabel destinationPanelLabel;
    private JTextField destinationPanelTextField;
    private JButton destinationPanelChooseButton;
    private JButton encryptButton;
    private JButton cancelButton;

    private final File targetItem;
    private String destinationPath;
    private EncryptionAlgorithm selectedAlgorithm = EncryptionAlgorithm.AES;
    private static final String[] ALGORITHM_OPTIONS = { "AES", "Blowfish", "Twofish" };
    private static final String[] ALGORITHM_DESCRIPTIONS = {
            LocalizationUtility.getLocalizedString("label.aes.description"),
            LocalizationUtility.getLocalizedString("label.blowfish.description"),
            LocalizationUtility.getLocalizedString("label.twofish.description")
    };

    public EncryptRoute(@NotNull File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        if (targetItem.isDirectory()) destinationPath = targetItem.getPath() + ".encd";
        else destinationPath = targetItem.getPath() + ".enc";

        destinationPanelTextField.setText(destinationPath);
        openedItemPanelInner.add(new OpenedItemLabel(targetItem));
        algorithmPanelDescription.setText(ALGORITHM_DESCRIPTIONS[0]);
        SwingUtilities.invokeLater(() -> algorithmPanelDescription.revalidate());

        algorithmPanelComboBox.addActionListener(this::handleAlgorithmPanelComboBoxAction);
        keygenPanelGenerateButton.addActionListener(this::handleKeygenPanelGenerateButtonAction);
        destinationPanelChooseButton.addActionListener(this::handleDestinationPanelChooseButtonAction);
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
        switch (selectedIndex) {
            case 0 -> selectedAlgorithm = EncryptionAlgorithm.AES;
            case 1 -> selectedAlgorithm = EncryptionAlgorithm.BLOWFISH;
            default -> selectedAlgorithm = EncryptionAlgorithm.TWOFISH;
        }
        algorithmPanelDescription.setText(ALGORITHM_DESCRIPTIONS[selectedIndex]);
        SwingUtilities.invokeLater(() -> algorithmPanelDescription.revalidate());
    }

    private void handleKeygenPanelGenerateButtonAction(ActionEvent event) {
        int keySizeBytes = 32;
        String key = generateSecureKey(keySizeBytes);
        keygenPanelTextField.setText(key);
        String key = UserInputHandler.generateRandomKey();
        keygenPanelTextField.setText(key);
    }

    private void handleDestinationPanelChooseButtonAction(ActionEvent event) {
        var fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choose the filename to be created");
        fileChooser.setApproveButtonText("Choose");
        int chooserResult = fileChooser.showSaveDialog(contentPane);
        if (chooserResult != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile.getPath().equals(targetItem.getPath())) {
            JOptionPane.showMessageDialog(contentPane, "You cannot choose target file as the destination");
            return;

        } else if (selectedFile.exists()) {
            int confirmationResult = JOptionPane.showConfirmDialog(
                    contentPane,
                    "The specified file already exists. Do you want to override it?",
                    "Override",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmationResult == JOptionPane.NO_OPTION) return;
        }

        destinationPath = selectedFile.getPath();
        destinationPanelTextField.setText(destinationPath);
    }

    private void handleEncryptButtonAction(ActionEvent event) {
        var mainFrame = MainFrame.getInstance();
        mainFrame.showNotification(LocalizationUtility.getLocalizedString("notification.item.encrypted"));
        mainFrame.addToRecentItems(targetItem);
        mainFrame.navigate(new StartRoute());
        String key = keygenPanelTextField.getText().trim();
        if (key.isEmpty()) {
            mainFrame.showNotification("The key cannot be empty.");
            return;
        }
        ICryptoService cryptoService = switch (selectedAlgorithm) {
            case AES -> new AesCryptoService();
            case BLOWFISH ->  new BlowfishCryptoService();
            case TWOFISH -> new TwofishCryptoService();
        };
        Path targetPath = targetItem.toPath();
        UserInputHandler handler = new UserInputHandler(cryptoService, key, targetPath);
        boolean success = handler.performOperation("encrypt", Path.of(destinationPath));
        if (success) {
            mainFrame.showNotification("Item encrypted successfully");
            mainFrame.addToRecentItems(new File(destinationPath));
            mainFrame.navigate(new StartRoute());
        } else {
            mainFrame.showNotification("An error occurred while encrypting the element.");
        }
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}