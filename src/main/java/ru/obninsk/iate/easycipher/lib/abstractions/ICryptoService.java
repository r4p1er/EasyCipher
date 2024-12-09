package ru.obninsk.iate.easycipher.lib.abstractions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ICryptoService {
    boolean encryptFile(Path filePath, String key, Path out);

    boolean decryptFile(Path filePath, String key, Path out);

    boolean encryptDirectory(Path dirPath, String key, Path out);

    boolean decryptDirectory(Path dirPath, String key, Path out);

    default boolean encryptFile(@NotNull Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".enc";
        return encryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    default boolean decryptFile(@NotNull Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".dec";
        return decryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    default boolean encryptDirectory(@NotNull Path directoryPath, String key) {
        var newFileName = directoryPath.getFileName().toString() + ".encd";
        return encryptDirectory(directoryPath, key, directoryPath.resolveSibling(newFileName));
    }

    default boolean decryptDirectory(@NotNull Path dirPath, String key) {
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
