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
    private static final String PADDING = "PKCS7Padding";
    private static final String TRANSFORMATION = String.join("/", ALGORITHM, MODE, PADDING);
    private static final String PROVIDER = "BC";
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 8;
    private static final int READ_BUFFER_SIZE = 4096;
    private static final String TEMP_ZIP_PREFIX = "temp-";
    private static final String TEMP_ZIP_SUFFIX = ".zip";

    @Override
    public boolean encryptFile(@NotNull Path filePath, String key, @NotNull Path out) {
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
            byte[] paddedIv = new byte[16]; // the length of 16 is hardcoded but needs to be 8 for this algorithm
            System.arraycopy(iv, 0, paddedIv, 0, IV_LENGTH);
            metadata.setIv(paddedIv);
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
    public boolean decryptFile(@NotNull Path filePath, String key, @NotNull Path out) {
        try (var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             var outputStream = new BufferedOutputStream(Files.newOutputStream(out))) {

            var messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);

            var metadata = new MetadataBlockService();
            if (!metadata.read(filePath)) throw new IOException("Failed to read metadata.");

            String algorithm = metadata.getAlgorithm();
            String mode = metadata.getMode();
            String padding = metadata.getPadding();
            String transformation = String.join("/", algorithm, mode, padding);
            byte[] paddedIv = metadata.getIv();
            byte[] iv = new byte[IV_LENGTH]; // the length of 16 is hardcoded but needs to be 8 for this algorithm
            System.arraycopy(paddedIv, 0, iv, 0, IV_LENGTH);

            byte[] keyBytes = Arrays.copyOf(key.getBytes(), KEY_LENGTH);
            var secretKey = new SecretKeySpec(keyBytes, algorithm);

            var ivSpec = new IvParameterSpec(iv);

            var cipher = Cipher.getInstance(transformation, PROVIDER);
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
    public boolean encryptDirectory(@NotNull Path directoryPath, String key, @NotNull Path out) {
        Path temporaryZipPath = null;

        try {
            temporaryZipPath = Files.createTempFile(TEMP_ZIP_PREFIX, TEMP_ZIP_SUFFIX);

            if (!ZipUtility.createZip(directoryPath, temporaryZipPath))
                throw new IOException("Failed to create ZIP archive.");
            if (!encryptFile(temporaryZipPath, key, out))
                throw new IOException("Failed to encrypt ZIP archive.");

            return true;

        } catch (Exception e) {
            return false;

        } finally {
            try {
                if (temporaryZipPath != null) Files.deleteIfExists(temporaryZipPath);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean decryptDirectory(@NotNull Path encryptedPath, String key, @NotNull Path outDir) {
        Path tempZipPath = null;

        try {
            tempZipPath = Files.createTempFile(TEMP_ZIP_PREFIX, TEMP_ZIP_SUFFIX);

            if (!decryptFile(encryptedPath, key, tempZipPath))
                throw new IOException("Failed to decrypt archive.");
            if (!ZipUtility.extractZip(tempZipPath, outDir))
                throw new IOException("Failed to extract ZIP archive.");

            return true;

        } catch (Exception e) {
            return false;

        } finally {
            try {
                if (tempZipPath != null) Files.deleteIfExists(tempZipPath);
            } catch (Exception ignored) {}
        }
    }
}
