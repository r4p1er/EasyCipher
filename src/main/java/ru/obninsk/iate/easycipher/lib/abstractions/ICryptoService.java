package ru.obninsk.iate.easycipher.lib.abstractions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ICryptoService {
    boolean encryptFile(Path filePath, String key, Path out);

    boolean encryptFile(Path filePath, String key);

    boolean decryptFile(Path filePath, String key, Path out);

    boolean decryptFile(Path filePath, String key);

    boolean encryptDirectory(Path dirPath, String key, Path out);

    boolean encryptDirectory(Path dirPath, String key);

    boolean decryptDirectory(Path dirPath, String key, Path out);

    default boolean decryptDirectory(Path dirPath, String key) {
        try {
            Path parent = dirPath.getParent();
            String fileNameWithoutExtension = dirPath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            Path targetDir = parent.resolve(fileNameWithoutExtension);
            Files.createDirectories(targetDir);

            return decryptDirectory(dirPath, key, targetDir);
        } catch (IOException e) {
            return false;
        }
    }
}
