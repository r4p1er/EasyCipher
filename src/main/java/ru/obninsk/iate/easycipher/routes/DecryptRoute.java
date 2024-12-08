package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.enums.EncryptionAlgorithm;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
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
    private static final String[] ALGORITHM_OPTIONS = { "AES", "Blowfish", "Twofish" };

    public DecryptRoute(@NotNull File targetItem) {
        super("EasyCipher - " + targetItem.getName());
        this.targetItem = targetItem;
        if (targetItem.isDirectory()) destinationPath = targetItem.getPath() + ".decd";
        else destinationPath = targetItem.getPath() + ".dec";

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
        mainFrame.showNotification("Item decrypted successfully");
        mainFrame.addToRecentItems(targetItem);
        mainFrame.navigate(new StartRoute());
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}
