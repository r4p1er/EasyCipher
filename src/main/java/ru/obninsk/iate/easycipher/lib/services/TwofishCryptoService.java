package ru.obninsk.iate.easycipher.lib.services;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.obninsk.iate.easycipher.lib.abstractions.ICryptoService;
import ru.obninsk.iate.easycipher.lib.abstractions.IMetadataBlockService;
import ru.obninsk.iate.easycipher.lib.utils.ZipUtility;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class TwofishCryptoService implements ICryptoService {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "Twofish";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS7Padding";
    private static final String TRANSFORMATION = String.join("/", ALGORITHM, MODE, PADDING);
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 16;

    @Override
    public boolean encryptFile(Path filePath, String key, Path out) {
        boolean error = false;

        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {

            var messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), KEY_LENGTH);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            AlgorithmParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalRead += bytesRead;
                messageDigest.update(buffer, 0, bytesRead);
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) {
                    outputStream.write(encrypted);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length > 0) {
                outputStream.write(finalBytes);
            }

            IMetadataBlockService metadata = new MetadataBlockService();
            metadata.setAlgorithm(ALGORITHM);
            metadata.setMode(MODE);
            metadata.setPadding(PADDING);
            metadata.setIv(iv);
            metadata.setDataLength(totalRead);
            metadata.setDataHash(messageDigest.digest());

            if (!metadata.write(outputStream))
                throw new IOException("Failed to write metadata.");

        } catch (Exception e) {
            try {
                Files.deleteIfExists(out);
            } catch (Exception ignored) {}

            error = true;
        }

        return !error;
    }

    @Override
    public boolean encryptFile(Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".enc";

        return encryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    @Override
    public boolean decryptFile(Path filePath, String key, Path out) {
        boolean error = false;

        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {

            var messageDigest = MessageDigest.getInstance("SHA-256");

            IMetadataBlockService metadata = new MetadataBlockService();
            if (!metadata.read(filePath)) throw new IOException("Failed to read metadata.");

            String algorithm = metadata.getAlgorithm();
            String mode = metadata.getMode();
            String padding = metadata.getPadding();
            String transformation = String.join("/", algorithm, mode, padding);

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), KEY_LENGTH);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, algorithm);

            AlgorithmParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(metadata.getIv());

            Cipher cipher = Cipher.getInstance(transformation, "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            long dataSize = Files.size(filePath) - metadata.getBlockLength();
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalDecryptedBytes = 0;

            while (totalDecryptedBytes < dataSize && (bytesRead = inputStream.read(buffer)) != -1) {
                int bytesToProcess = (int) Math.min(bytesRead, dataSize - totalDecryptedBytes);
                byte[] decrypted = cipher.update(buffer, 0, bytesToProcess);
                if (decrypted != null) {
                    messageDigest.update(decrypted);
                    outputStream.write(decrypted);
                    totalDecryptedBytes += decrypted.length;
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
                    metadata.getDataLength() != totalDecryptedBytes) {
                throw new IOException("Data integrity check failed.");
            }

        } catch (Exception e) {
            try {
                Files.deleteIfExists(out);
            } catch (Exception ignored) {
            }
            error = true;
        }

        return !error;
    }

    @Override
    public boolean decryptFile(Path filePath, String key) {
        var newFileName = filePath.getFileName().toString() + ".dec";

        return decryptFile(filePath, key, filePath.resolveSibling(newFileName));
    }

    @Override
    public boolean encryptDirectory(Path directoryPath, String key, Path out) {
        boolean error = false;
        Path temporaryZip = null;

        try {
            temporaryZip = Files.createTempFile("temp-", ".zip");
            if (!ZipUtility.createZip(directoryPath, temporaryZip))
                throw new IOException("Failed to create ZIP archive.");

            if (!encryptFile(temporaryZip, key, out))
                throw new IOException("Failed to encrypt ZIP archive.");

        } catch (Exception e) {
            try { Files.deleteIfExists(out); } catch (Exception ignored) {}
            error = true;
        } finally {
            try {
                if (temporaryZip != null) Files.deleteIfExists(temporaryZip);
            } catch (Exception ignored) {
            }
        }

        return !error;
    }

    @Override
    public boolean encryptDirectory(Path directoryPath, String key) {
        var newFileName = directoryPath.getFileName().toString() + ".encd";

        return encryptDirectory(directoryPath, key, directoryPath.resolveSibling(newFileName));
    }

    @Override
    public boolean decryptDirectory(Path encryptedPath, String key, Path outDir) {
        boolean error = false;
        Path tempZip = null;

        try {
            tempZip = Files.createTempFile("temp-", ".zip");
            if (!decryptFile(encryptedPath, key, tempZip)) throw new IOException("Failed to decrypt archive.");

            if (!ZipUtility.extractZip(tempZip, outDir)) throw new IOException("Failed to extract ZIP archive.");
        } catch (Exception e) {
            try { Files.deleteIfExists(outDir); } catch (Exception ignored) {}
            error = true;
        } finally {
            try {
                if (tempZip != null) Files.deleteIfExists(tempZip);
            } catch (Exception ignored) {
            }
        }

        return !error;
    }
}
