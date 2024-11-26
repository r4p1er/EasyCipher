package ru.obninsk.iate.easycipher.routes;

import javax.swing.*;
import java.io.File;

public class EncryptRoute extends Route {
    private JPanel contentPane;
    private JLabel testLabel;
    private final File targetFile;

    public EncryptRoute(File targetFile) {
        super("EasyCipher - " + targetFile.getName());
        this.targetFile = targetFile;
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }
}
