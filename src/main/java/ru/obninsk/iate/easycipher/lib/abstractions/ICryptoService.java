package ru.obninsk.iate.easycipher.lib.abstractions;

import java.nio.file.Path;

public interface ICryptoService {
    boolean encryptFile(Path filePath, String key, Path out);

    boolean encryptFile(Path filePath, String key);

    boolean decryptFile(Path filePath, String key, Path out);

    boolean decryptFile(Path filePath, String key);

    boolean encryptDirectory(Path dirPath, String key, Path out);

    boolean encryptDirectory(Path dirPath, String key);

    boolean decryptDirectory(Path dirPath, String key, Path out);

    boolean decryptDirectory(Path dirPath, String key);
}
