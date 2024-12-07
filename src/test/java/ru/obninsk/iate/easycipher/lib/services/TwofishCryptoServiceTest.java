package ru.obninsk.iate.easycipher.lib.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.net.URL;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

public class TwofishCryptoServiceTest {

    private final TwofishCryptoService cryptoService = new TwofishCryptoService();
    private Path originalFile;
    private Path encryptedFile;
    private Path decryptedFile;

    private Path originalDir;
    private Path encryptedDir;
    private Path decryptedDir;

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

        originalDir = testFilesDir.resolve("original_dir");
        encryptedDir = testFilesDir.resolve("original_dir.encd");
        decryptedDir = testFilesDir.resolve("original_dir.dec");

        if (!Files.exists(testFilesDir)) {
            Files.createDirectories(testFilesDir);
        }

        if (!Files.exists(originalFile)) {
            Files.writeString(originalFile, "test_string_123 456");
        }

        if (!Files.exists(originalDir)) {
            Files.createDirectories(originalDir);
            Files.writeString(originalDir.resolve("file1.txt"), "Content of file 1");
            Files.writeString(originalDir.resolve("file2.txt"), "Content of file 2");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(encryptedFile);
        Files.deleteIfExists(decryptedFile);

        if (Files.exists(decryptedDir)) {
            try (Stream<Path> paths = Files.walk(decryptedDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                System.err.println("Не удалось удалить файл: " + file.getAbsolutePath());
                            }
                        });
            } catch (IOException e) {
                System.err.println("Ошибка при обработке каталога: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testEncryptAndDecryptFile() throws Exception {
        String key = "MySecretKey1234567890";

        boolean encryptSuccess = cryptoService.encryptFile(originalFile, key, encryptedFile);
        assertTrue(encryptSuccess, "Шифрование файла должно пройти успешно");

        boolean decryptSuccess = cryptoService.decryptFile(encryptedFile, key, decryptedFile);
        assertTrue(decryptSuccess, "Дешифрование файла должно пройти успешно");

        String originalContent = Files.readString(originalFile);
        String decryptedContent = Files.readString(decryptedFile);
        assertEquals(originalContent, decryptedContent, "Содержимое файлов должно совпадать после дешифрования");

        byte[] encryptedBytes = Files.readAllBytes(encryptedFile);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Начальное содержимое файла: " + originalContent);
        System.out.println("Зашифрованное содержимое файла (Base64): " + encryptedBase64);
        System.out.println("Расшифрованное содержимое файла: " + decryptedContent);
    }

    @Test
    public void testEncryptAndDecryptDirectory() throws Exception {
        String key = "MySecretKey1234567890";

        Path originalFile1 = originalDir.resolve("file1.txt");
        Path originalFile2 = originalDir.resolve("file2.txt");
        String originalContent1 = Files.readString(originalFile1);
        String originalContent2 = Files.readString(originalFile2);

        boolean encryptSuccess = cryptoService.encryptDirectory(originalDir, key, encryptedDir);
        assertTrue(encryptSuccess, "Шифрование директории должно пройти успешно");

        boolean decryptSuccess = cryptoService.decryptDirectory(encryptedDir, key, decryptedDir);
        assertTrue(decryptSuccess, "Дешифрование директории должно пройти успешно");

        Path decryptedFile1 = decryptedDir.resolve("file1.txt");
        Path decryptedFile2 = decryptedDir.resolve("file2.txt");
        assertTrue(Files.exists(decryptedFile1), "Дешифрованный файл1 должен существовать");
        assertTrue(Files.exists(decryptedFile2), "Дешифрованный файл2 должен существовать");

        String decryptedContent1 = Files.readString(decryptedFile1);
        String decryptedContent2 = Files.readString(decryptedFile2);
        assertEquals("Content of file 1", decryptedContent1, "Содержимое file1.txt должно совпадать после дешифрования");
        assertEquals("Content of file 2", decryptedContent2, "Содержимое file2.txt должно совпадать после дешифрования");

        System.out.println();
        System.out.println("Содержимое файлов в директории до шифрования:");
        System.out.println("file1.txt: " + originalContent1);
        System.out.println("file2.txt: " + originalContent2);
        System.out.println("Содержимое файлов в директории после дешифрования:");
        System.out.println("file1.txt: " + decryptedContent1);
        System.out.println("file2.txt: " + decryptedContent2);
    }
}
