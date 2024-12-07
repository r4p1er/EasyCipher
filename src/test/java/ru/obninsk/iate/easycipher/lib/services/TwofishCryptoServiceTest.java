package ru.obninsk.iate.easycipher.lib.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.net.URL;
import java.util.Base64;

public class TwofishCryptoServiceTest {

    private final TwofishCryptoService cryptoService = new TwofishCryptoService();
    private Path originalFile;
    private Path encryptedFile;
    private Path decryptedFile;

    @BeforeEach
    public void setUp() throws Exception {
        URL resource = getClass().getResource("");
        if (resource == null) {
            throw new IOException("Не удалось определить директорию тестового класса.");
        }
        Path testClassDir = Paths.get(resource.toURI());
        Path testFilesDir = testClassDir.resolve("test_files");

        originalFile = testFilesDir.resolve("original.txt");
        encryptedFile = testFilesDir.resolve("original.txt.enc");
        decryptedFile = testFilesDir.resolve("original.txt.dec");

        if (!Files.exists(testFilesDir)) {
            Files.createDirectories(testFilesDir);
        }

        if (!Files.exists(originalFile)) {
            Files.writeString(originalFile, "test_string_123 456");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(encryptedFile);
        Files.deleteIfExists(decryptedFile);
    }

    @Test
    public void testEncryptAndDecryptFile() throws Exception {
        String key = "MySecretKey1234567890";
        boolean encryptSuccess = cryptoService.encryptFile(originalFile, key, encryptedFile);
        assertTrue(encryptSuccess, "Шифрование должно пройти успешно");

        boolean decryptSuccess = cryptoService.decryptFile(encryptedFile, key, decryptedFile);
        assertTrue(decryptSuccess, "Дешифрование должно пройти успешно");

        String originalContent = Files.readString(originalFile);
        String decryptedContent = Files.readString(decryptedFile);
        assertEquals(originalContent, decryptedContent, "Содержимое файлов должно совпадать после дешифрования");

        byte[] encryptedBytes = Files.readAllBytes(encryptedFile);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Начальное содержимое: " + originalContent);
        System.out.println("Зашифрованное содержимое (Base64): " + encryptedBase64);
        System.out.println("Расшифрованное содержимое: " + decryptedContent);
    }
}
