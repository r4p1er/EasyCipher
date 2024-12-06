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
        var mainFrame = MainFrame.getInstance();
        mainFrame.showNotification("Item decrypted successfully");
        mainFrame.addToRecentItems(targetItem);
        mainFrame.navigate(new StartRoute());
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}
