package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.enums.Algorithm;
import ru.obninsk.iate.easycipher.lib.utils.LocalizationUtility;

import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;
import ru.obninsk.iate.easycipher.lib.abstractions.IMetadataBlockService;
import ru.obninsk.iate.easycipher.lib.enums.EncryptionAlgorithm;
import ru.obninsk.iate.easycipher.lib.services.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.nio.file.Path;
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
    private JPanel destinationPanel;
    private JTextField destinationPanelTextField;
    private JLabel destinationPanelLabel;
    private JButton destinationPanelChooseButton;
    private JButton decryptButton;
    private JButton cancelButton;

    private final File targetItem;
    private String destinationPath;
    private EncryptionAlgorithm selectedAlgorithm = EncryptionAlgorithm.AES;
    private Boolean autoAlgorithmDetection = false;
    private static final String[] ALGORITHM_OPTIONS = {"AES", "Blowfish", "Twofish"};

    public DecryptRoute(@NotNull File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        destinationPath = targetItem.getPath() + ".dec";

        destinationPanelTextField.setText(destinationPath);
        openedItemPanelInner.add(new OpenedItemLabel(targetItem));

        algorithmPanelComboBox.addActionListener(this::handleAlgorithmPanelComboBoxAction);
        algorithmPanelAutoCheckBox.addItemListener(this::handleAlgorithmPanelAutoCheckBoxChange);
        destinationPanelChooseButton.addActionListener(this::handleDestinationPanelChooseButtonAction);
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

    private void handleDestinationPanelChooseButtonAction(ActionEvent event) {
        var fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choose the path to be created");
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
                    "Override the file?",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmationResult == JOptionPane.NO_OPTION) return;
        }

        destinationPath = selectedFile.getPath();
        destinationPanelTextField.setText(destinationPath);
    }

    private void handleDecryptButtonAction(ActionEvent event) {
        var mainFrame = MainFrame.getInstance();
        String key = keyPanelTextField.getText().trim();
        if (key.isEmpty()) {
            mainFrame.showNotification("The key cannot be empty.");
            return;
        }

        ICryptoService cryptoService;
        if (!autoAlgorithmDetection) {
            IMetadataBlockService metadataService = new MetadataBlockService();
            boolean metadataRead = metadataService.read(targetItem.toPath());
            if (!metadataRead) {
                mainFrame.showNotification("Failed to read metadata from the file.");
                return;
            }

            String actualAlgorithm = metadataService.getAlgorithm();
            if (actualAlgorithm == null || actualAlgorithm.isEmpty()) {
                mainFrame.showNotification("No algorithm found in metadata.");
                return;
            }

            if (!actualAlgorithm.equalsIgnoreCase(selectedAlgorithm.name())) {
                mainFrame.showNotification("Selected file was encrypted using " + actualAlgorithm +
                                ", but you selected: " + selectedAlgorithm.name()
                );
                return;
            }

            cryptoService = switch (selectedAlgorithm) {
                case AES -> new AesCryptoService();
                case BLOWFISH -> new BlowfishCryptoService();
                case TWOFISH -> new TwofishCryptoService();
            };
        } else {
            cryptoService = detectCryptoService(targetItem.toPath());
            if (cryptoService == null) {
                mainFrame.showNotification("Failed to detect the encryption algorithm.");
                return;
            }
        }

        Path targetPath = targetItem.toPath();
        UserInputHandler handler = new UserInputHandler(cryptoService, key, targetPath);
        boolean success = handler.performOperation("decrypt", Path.of(destinationPath));
        if (success) {
            mainFrame.showNotification(LocalizationUtility.getLocalizedString("notification.item.decrypted"));
            mainFrame.addToRecentItems(targetItem);
            mainFrame.navigate(new StartRoute());
        } else {
            mainFrame.showNotification("Failed to decrypt the item");
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
            case "BLOWFISH" -> new BlowfishCryptoService();
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