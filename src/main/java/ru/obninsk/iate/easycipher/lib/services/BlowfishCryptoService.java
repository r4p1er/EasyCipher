package ru.obninsk.iate.easycipher.lib.services;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;
import ru.obninsk.iate.easycipher.lib.utils.ZipUtility;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

public class BlowfishCryptoService implements ICryptoService {
    static { Security.addProvider(new BouncyCastleProvider()); }

    private static final String ALGORITHM = "Blowfish";
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String MODE = "CBC";
    private static final String PROVIDER = "BC";
    private static final String PADDING = "PKCS7Padding";
    private static final String TRANSFORMATION = String.join("/", ALGORITHM, MODE, PADDING);
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 16;
    private static final int READ_BUFFER_SIZE = 4096;

    @Override
    public boolean encryptFile(Path filePath, String key, Path out) {
        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {

            var messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), KEY_LENGTH);
            var secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            var ivSpec = new IvParameterSpec(iv);

            var cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[READ_BUFFER_SIZE];
            int bytesRead;
            long totalRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalRead += bytesRead;
                messageDigest.update(buffer, 0, bytesRead);
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) outputStream.write(encrypted);
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length > 0)
                outputStream.write(finalBytes);

            var metadata = new MetadataBlockService();
            metadata.setAlgorithm(ALGORITHM);
            metadata.setMode(MODE);
            metadata.setPadding(PADDING);
            metadata.setIv(iv);
            metadata.setDataLength(totalRead);
            metadata.setDataHash(messageDigest.digest());

            if (!metadata.write(outputStream))
                throw new IOException("Failed to write metadata.");

            return true;

        } catch (Exception e) {
            try { Files.deleteIfExists(out); } catch (Exception ignored) {}
            return false;
        }
    }

    @Override
    public boolean encryptFile(@NotNull Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".enc";
        return encryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    @Override
    public boolean decryptFile(Path filePath, String key, Path out) {
        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {

            var messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);

            var metadata = new MetadataBlockService();
            if (!metadata.read(filePath)) throw new IOException("Failed to read metadata.");

            String algorithm = metadata.getAlgorithm();
            String mode = metadata.getMode();
            String padding = metadata.getPadding();
            String transformation = String.join("/", algorithm, mode, padding);

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), KEY_LENGTH);
            var secretKey = new SecretKeySpec(keyBytes, algorithm);

            var ivSpec = new javax.crypto.spec.IvParameterSpec(metadata.getIv());

            var cipher = Cipher.getInstance(transformation, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            long dataSize = Files.size(filePath) - metadata.getBlockLength();
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            int bytesRead;
            long totalDecryptedBytes = 0;

            while (totalDecryptedBytes < dataSize && (bytesRead = inputStream.read(buffer)) != -1) {
                int bytesToProcess = (int) Math.min(bytesRead, dataSize - totalDecryptedBytes);
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesToProcess);
                if (decryptedBytes != null) {
                    messageDigest.update(decryptedBytes);
                    outputStream.write(decryptedBytes);
                    totalDecryptedBytes += decryptedBytes.length;
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length > 0) {
                messageDigest.update(finalBytes);
                outputStream.write(finalBytes);
                totalDecryptedBytes += finalBytes.length;
            }

            byte[] computedHash = messageDigest.digest();
            if (!Arrays.equals(metadata.getDataHash(), computedHash) ||
                metadata.getDataLength() != totalDecryptedBytes
            ) throw new IOException("Data integrity check failed.");

            return true;

        } catch (Exception e) {
            try { Files.deleteIfExists(out); } catch (Exception ignored) {}
            return false;
        }
    }

    @Override
    public boolean decryptFile(@NotNull Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".dec";
        return decryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    @Override
    public boolean encryptDirectory(Path directoryPath, String key, Path out) {
        Path temporaryZipPath = null;

        try {
            temporaryZipPath = Files.createTempFile("temp-", ".zip");

            if (!ZipUtility.createZip(directoryPath, temporaryZipPath))
                throw new IOException("Failed to create ZIP archive.");
            if (!encryptFile(temporaryZipPath, key, out))
                throw new IOException("Failed to encrypt ZIP archive.");

            return true;

        } catch (Exception e) {
            try { Files.deleteIfExists(out); } catch (Exception ignored) {}
            return  false;

        } finally {
            try {
                if (temporaryZipPath != null) Files.deleteIfExists(temporaryZipPath);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean encryptDirectory(@NotNull Path directoryPath, String key) {
        var newFileName = directoryPath.getFileName().toString() + ".encd";
        return encryptDirectory(directoryPath, key, directoryPath.resolveSibling(newFileName));
    }

    @Override
    public boolean decryptDirectory(Path encryptedPath, String key, Path outDir) {
        Path tempZipPath = null;

        try {
            tempZipPath = Files.createTempFile("temp-", ".zip");

            if (!decryptFile(encryptedPath, key, tempZipPath))
                throw new IOException("Failed to decrypt archive.");
            if (!ZipUtility.extractZip(tempZipPath, outDir))
                throw new IOException("Failed to extract ZIP archive.");

            return true;

        } catch (Exception e) {
            try { Files.deleteIfExists(outDir); } catch (Exception ignored) {}
            return false;

        } finally {
            try {
                if (tempZipPath != null) Files.deleteIfExists(tempZipPath);
            } catch (Exception ignored) {}
        }
    }
}
