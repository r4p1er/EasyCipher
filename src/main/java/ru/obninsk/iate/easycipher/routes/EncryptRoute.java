package ru.obninsk.iate.easycipher.routes;

import org.jetbrains.annotations.NotNull;
import ru.obninsk.iate.easycipher.MainFrame;
import ru.obninsk.iate.easycipher.lib.Algorithm;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

public class EncryptRoute extends Route {
    private JPanel contentPane;
    private JPanel openedItemPanel;
    private JLabel openedItemPanelLabel;
    private JPanel algorithmPanel;
    private JLabel algorithmPanelLabel;
    private JComboBox<String> algorithmPanelComboBox;
    private JTextPane algorithmPanelDescription;
    private JPanel keygenPanel;
    private JLabel keygenPanelLabel;
    private JTextField keygenPanelTextField;
    private JButton keygenPanelGenerateButton;
    private JButton encryptButton;

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
        renderOpenedItemLabelPanel();
        algorithmPanelDescription.setText(ALGORITHM_DESCRIPTIONS[0]);
        algorithmPanelComboBox.addActionListener(this::handleAlgorithmPanelComboBoxAction);
        keygenPanelGenerateButton.addActionListener(this::handleKeygenPanelGenerateButtonAction);
        encryptButton.addActionListener(this::handleEncryptButtonAction);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    private void renderOpenedItemLabelPanel() {
        openedItemPanelLabel.setText(targetItem.getName());
        String iconAssetPath = targetItem.isDirectory()
                ? "/icons/opened-directory-icon.png"
                : "/icons/opened-file-icon.png";
        try {
            URL iconStream = EncryptRoute.class.getResource(iconAssetPath);
            if (iconStream == null) throw new Exception();

            var icon = new ImageIcon(iconStream);
            openedItemPanelLabel.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Error occurred while loading icon");
        }
    }

    private void createUIComponents() {
        algorithmPanelComboBox = new JComboBox<>(ALGORITHM_OPTIONS);
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
        MainFrame.getInstance().navigate(new StartRoute());
    }
}
