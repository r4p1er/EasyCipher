package ru.obninsk.iate.easycipher.lib.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtility {
    private ZipUtility() {
        throw new UnsupportedOperationException();
    }

    public static boolean createZip(Path sourceDir, Path zipFilePath) {
        boolean error = false;

        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zipOutputStream.putNextEntry(new ZipEntry(sourceDir.relativize(file).toString()));
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ignored) {
            try {
                Files.deleteIfExists(zipFilePath);
            } catch (Exception ignored2) {}

            error = true;
        }

        return !error;
    }

    public static boolean createZip(Path sourceDir) {
        var newFileName = sourceDir.getFileName().toString() + ".zip";

        return createZip(sourceDir, sourceDir.resolveSibling(newFileName));
    }

    public static boolean extractZip(Path zipFilePath, Path targetDir) {
        boolean error = false;

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path extractedPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    Files.createDirectories(extractedPath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(extractedPath)) {
                        zipInputStream.transferTo(outputStream);
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception ignored) {
            error = true;
        }

        return !error;
    }

    public static boolean extractZip(Path zipFilePath) {
        try {
            Path parent = zipFilePath.getParent();
            if (parent == null) return false;
            String fileNameWithoutExtension = zipFilePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            Path targetDir = parent.resolve(fileNameWithoutExtension);
            Files.createDirectories(targetDir);

            return extractZip(zipFilePath, targetDir);
        } catch (IOException e) {
            return false;
        }
    }
}
