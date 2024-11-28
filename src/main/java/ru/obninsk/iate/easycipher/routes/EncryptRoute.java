package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.*;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.components.OpenedItemLabel;
import ru.obninsk.iate.easycipher.lib.Algorithm;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

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
            "AES Description, AES Description, AES Description, AES Description, AES Description.",
            "Blowfish Description, Blowfish Description, Blowfish Description, Blowfish Description.",
            "Twofish Description, Twofish Description, Twofish Description, Twofish Description.",
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
        keygenPanelTextField.setText("Very secure and, of course, random key");
    }

    private void handleEncryptButtonAction(ActionEvent event) {
        var mainFrame = MainFrame.getInstance();
        mainFrame.showNotification("Item encrypted successfully");
        mainFrame.addToRecentItems(targetItem);
        mainFrame.navigate(new StartRoute());
    }

    private void handleCancelButtonAction(ActionEvent event) {
        MainFrame.getInstance().navigate(new StartRoute());
    }
}
