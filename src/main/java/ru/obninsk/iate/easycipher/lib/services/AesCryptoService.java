package ru.obninsk.iate.easycipher.lib.services;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
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

public class AesCryptoService implements ICryptoService {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public boolean encryptFile(@NotNull Path filePath, String key, @NotNull Path out) {
        boolean error = false;

        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {
            var messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), 32);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            AlgorithmParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalRead += bytesRead;
                messageDigest.update(buffer, 0, bytesRead);
                byte[] encrypted = cipher.update(buffer, 0, bytesRead);
                if (encrypted != null) outputStream.write(encrypted);
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length > 0) {
                outputStream.write(finalBytes);
            }

            IMetadataBlockService metadata = new MetadataBlockService();
            metadata.setAlgorithm("AES");
            metadata.setMode("CBC");
            metadata.setPadding("PKCS7Padding");
            metadata.setIv(iv);
            metadata.setDataLength(totalRead);
            metadata.setDataHash(messageDigest.digest());

            if (!metadata.write(outputStream)) throw new IOException();
        } catch (Exception ignored) {
            try {
                Files.deleteIfExists(out);
            } catch (Exception ignored2) {}

            error = true;
        }

        return !error;
    }

    @Override
    public boolean decryptFile(@NotNull Path filePath, String key, @NotNull Path out) {
        boolean error = false;

        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {
            var messageDigest = MessageDigest.getInstance("SHA-256");
            IMetadataBlockService metadata = new MetadataBlockService();
            if (!metadata.read(filePath)) throw new IOException();

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), 32);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, metadata.getAlgorithm());

            AlgorithmParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(metadata.getIv());

            Cipher cipher = Cipher.getInstance(metadata.getAlgorithm() + "/" + metadata.getMode() + "/" + metadata.getPadding(), "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0, totalDecryptedBytes = 0, dataSize = Files.size(filePath) - metadata.getBlockLength();

            while (totalBytesRead < dataSize && (bytesRead = inputStream.read(buffer)) != -1) {
                int bytesToProcess = (int) Math.min(bytesRead, dataSize - totalBytesRead);
                byte[] decrypted = cipher.update(buffer, 0, bytesToProcess);
                totalDecryptedBytes += decrypted.length;
                messageDigest.update(decrypted);
                outputStream.write(decrypted);
                totalBytesRead += bytesToProcess;
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null && finalBytes.length > 0) {
                messageDigest.update(finalBytes);
                outputStream.write(finalBytes);
                totalDecryptedBytes += finalBytes.length;
            }

            if (!Arrays.equals(metadata.getDataHash(), messageDigest.digest()) || metadata.getDataLength() != totalDecryptedBytes) throw new IOException();
        } catch (Exception ignored) {
            try {
                Files.deleteIfExists(out);
            } catch (Exception ignored2) {}

            error = true;
        }

        return !error;
    }

    @Override
    public boolean encryptDirectory(@NotNull Path dirPath, String key, @NotNull Path out) {
        boolean error = false;
        Path tempZip = null;

        try {
            tempZip = Files.createTempFile("temp-", ".zip");
            if (!ZipUtility.createZip(dirPath, tempZip)) throw new IOException();
            if (!encryptFile(tempZip, key, out)) throw new IOException();
        } catch (Exception e) {
            error = true;
        } finally {
            try {
                if (tempZip != null) Files.deleteIfExists(tempZip);
            } catch (Exception ignored) {}
        }

        return !error;
    }

    @Override
    public boolean decryptDirectory(@NotNull Path encdPath, String key, @NotNull Path outDir) {
        boolean error = false;
        Path tempZip = null;

        try {
            tempZip = Files.createTempFile("temp-", ".zip");
            if (!decryptFile(encdPath, key, tempZip)) throw new IOException();
            if (!ZipUtility.extractZip(tempZip, outDir)) throw new IOException();
        } catch (Exception e) {
            error = true;
        } finally {
            try {
                if (tempZip != null) Files.deleteIfExists(tempZip);
            } catch (Exception ignored) {}
        }

        return !error;
    }
}
