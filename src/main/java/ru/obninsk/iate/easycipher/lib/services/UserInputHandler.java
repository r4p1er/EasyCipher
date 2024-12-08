package ru.obninsk.iate.easycipher.lib.services;

import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;

import java.security.SecureRandom;
import java.util.Base64;
import java.nio.file.Path;

public class UserInputHandler {
    private final ICryptoService cryptoService;
    private final String key;
    private final Path objectPath;

    public UserInputHandler(ICryptoService cryptoService, String key, Path objectPath) {
        this.cryptoService = cryptoService;
        this.key = key;
        this.objectPath = objectPath;
    }

    public boolean performOperation(String operation, Path outputPath) {
        try {
            switch (operation.toLowerCase()) {
                case "encrypt":
                    if (objectPath.toFile().isDirectory()) {
                        return cryptoService.encryptDirectory(objectPath, key, outputPath);
                    } else {
                        return cryptoService.encryptFile(objectPath, key, outputPath);
                    }
                case "decrypt":
                    if (objectPath.toFile().isDirectory()) {
                        return cryptoService.decryptDirectory(objectPath, key, outputPath);
                    } else {
                        return cryptoService.decryptFile(objectPath, key, outputPath);
                    }
                default:
                    throw new IllegalArgumentException("Invalid operation type: " + operation);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean performOperation(String operation) {
        try {
            switch (operation.toLowerCase()) {
                case "encrypt":
                    if (objectPath.toFile().isDirectory()) {
                        return cryptoService.encryptDirectory(objectPath, key);
                    } else {
                        return cryptoService.encryptFile(objectPath, key);
                    }
                case "decrypt":
                    if (objectPath.toFile().isDirectory()) {
                        return cryptoService.decryptDirectory(objectPath, key);
                    } else {
                        return cryptoService.decryptFile(objectPath, key);
                    }
                default:
                    throw new IllegalArgumentException("Invalid operation type: " + operation);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String generateRandomKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public ICryptoService getCryptoService() {
        return cryptoService;
    }

    public String getKey() {
        return key;
    }
}
