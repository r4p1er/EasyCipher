package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;
import ru.obninsk.iate.easycipher.lib.abstractions.IMetadataBlockService;
import ru.obninsk.iate.easycipher.lib.enums.EncryptionAlgorithm;
import ru.obninsk.iate.easycipher.lib.services.MetadataBlockService;
import ru.obninsk.iate.easycipher.lib.services.UserInputHandler;
import ru.obninsk.iate.easycipher.lib.services.AesCryptoService;
//TODO: после реализации вернуть import ru.obninsk.iate.easycipher.lib.services.BlowfishCryptoService;
import ru.obninsk.iate.easycipher.lib.services.TwofishCryptoService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class DecryptRoute extends Route {
    private JPanel contentPane;
    private JScrollPane contentScrollPane;
    private JPanel contentPaneInner;
    private JPanel openedItemPanel;
    private JPanel openedItemPanelInner;
    private JPanel algorithmPanel;
    private JLabel algorithmPanelLabel;
    private JComboBox<String> algorithmPanelComboBox;
    private JCheckBox algorithmPanelAutoCheckBox;
    private JPanel keyPanel;
    private JLabel keyPanelLabel;
    private JTextField keyPanelTextField;
    private JButton decryptButton;
    private JButton cancelButton;

    private final File targetItem;
    private EncryptionAlgorithm selectedAlgorithm = EncryptionAlgorithm.AES;
    private Boolean autoAlgorithmDetection = false;
    private static final String[] ALGORITHM_OPTIONS = { "AES", "Blowfish", "Twofish" };

    public DecryptRoute(@NotNull File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        openedItemPanelInner.add(new OpenedItemLabel(targetItem));
        algorithmPanelComboBox.addActionListener(this::handleAlgorithmPanelComboBoxAction);
        algorithmPanelAutoCheckBox.addItemListener(this::handleAlgorithmPanelAutoCheckBoxChange);
        decryptButton.addActionListener(this::handleDecryptButtonAction);
        cancelButton.addActionListener(this::handleCancelButtonAction);
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
    }

    private void handleAlgorithmPanelAutoCheckBoxChange(@NotNull ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            autoAlgorithmDetection = true;
            algorithmPanelComboBox.setEnabled(false);
        } else {
            autoAlgorithmDetection = false;
            algorithmPanelComboBox.setEnabled(true);
        }
    }

    private void handleDecryptButtonAction(ActionEvent event) {
        String key = keyPanelTextField.getText().trim();
        if (key.isEmpty()) {
            MainFrame.getInstance().showNotification("The key cannot be empty.");
            return;
        }
        ICryptoService cryptoService;
        if (!autoAlgorithmDetection) {
            cryptoService = switch (selectedAlgorithm) {
                case AES -> new AesCryptoService();
                case BLOWFISH -> null;//TODO: после реализации вернуть new BlowfishCryptoService();
                case TWOFISH -> new TwofishCryptoService();
            };
        } else {
            cryptoService = detectCryptoService(targetItem.toPath());
            if (cryptoService == null) {
                MainFrame.getInstance().showNotification("Failed to detect the encryption algorithm.");
                return;
            }
        }
        Path targetPath = targetItem.toPath();
        UserInputHandler handler = new UserInputHandler(cryptoService, key, targetPath);
        boolean success = handler.performOperation("decrypt");
        if (success) {
            MainFrame.getInstance().showNotification("Item decrypted successfully");
            MainFrame.getInstance().addToRecentItems(targetItem);
            MainFrame.getInstance().navigate(new StartRoute());
        } else {
            MainFrame.getInstance().showNotification("Failed to decrypt the item");
        }
    }

    private ICryptoService detectCryptoService(Path path) {
        IMetadataBlockService metadataService = new MetadataBlockService();
        boolean metadataRead = metadataService.read(path);
        if (!metadataRead) {
            return null;
        }

        String algorithm = metadataService.getAlgorithm();
        if (algorithm == null || algorithm.isEmpty()) {
            return null;
        }

        return switch (algorithm.toUpperCase()) {
            case "AES" -> new AesCryptoService();
            case "BLOWFISH" -> null; // TODO: после реализации вернуть new BlowfishCryptoService();
            case "TWOFISH" -> new TwofishCryptoService();
            default -> null;
        };
    }

    private String removeExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}