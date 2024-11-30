package ru.obninsk.iate.easycipher.lib.services;

import ru.obninsk.iate.easycipher.lib.abstractions.IMetadataBlockService;

import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;

public class MetadataBlockService implements IMetadataBlockService  {
    private String _algorithm = "";
    private String _mode = "";
    private String _padding = "";
    private byte[] _iv = new byte[16];
    private long _dataLength = 0;
    private byte[] _dataHash = new byte[32];

    @Override
    public boolean read(Path path) {
        try (var stream = new RandomAccessFile(path.toFile(), "r")) {
            stream.seek(stream.length() - 136);
            var messageDigest = MessageDigest.getInstance("SHA-256");
            var buffer = new byte[16];

            if (stream.read(buffer, 0, 16) != 16) return false;
            messageDigest.update(buffer, 0, 16);
            _algorithm = trimNullBytes(buffer);

            if (stream.read(buffer, 0, 16) != 16) return false;
            messageDigest.update(buffer, 0, 16);
            _mode = trimNullBytes(buffer);

            if (stream.read(buffer, 0, 16) != 16) return false;
            messageDigest.update(buffer, 0, 16);
            _padding = trimNullBytes(buffer);

            if (stream.read(_iv, 0, 16) != 16) return false;
            messageDigest.update(_iv, 0, 16);

            if (stream.read(buffer, 0, 8) != 8) return false;
            messageDigest.update(buffer, 0, 8);
            _dataLength = bytesToLong(buffer);

            if (stream.read(_dataHash, 0, 32) != 32) return false;
            messageDigest.update(_dataHash, 0, 32);

            var hashBytes = new byte[32];
            if (stream.read(hashBytes, 0, 32) != 32) return false;

            var hashResult = messageDigest.digest();

            return Arrays.equals(hashBytes, hashResult);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean write(OutputStream stream) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-256");

            var buffer = padNullBytes(_algorithm);
            messageDigest.update(buffer, 0, 16);
            stream.write(buffer, 0, 16);

            buffer = padNullBytes(_mode);
            messageDigest.update(buffer, 0, 16);
            stream.write(buffer, 0, 16);

            buffer = padNullBytes(_padding);
            messageDigest.update(buffer, 0, 16);
            stream.write(buffer, 0, 16);

            messageDigest.update(_iv, 0, 16);
            stream.write(_iv, 0, 16);

            buffer = longToBytes(_dataLength);
            messageDigest.update(buffer, 0, 8);
            stream.write(buffer, 0, 8);

            messageDigest.update(_dataHash, 0, 32);
            stream.write(_dataHash, 0, 32);

            buffer = messageDigest.digest();
            stream.write(buffer, 0, 32);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int getBlockLength() {
        return 136;
    }

    @Override
    public String getAlgorithm() {
        return _algorithm;
    }

    @Override
    public void setAlgorithm(String algorithm) {
        _algorithm = algorithm;
    }

    @Override
    public String getMode() {
        return _mode;
    }

    @Override
    public void setMode(String mode) {
        _mode = mode;
    }

    @Override
    public String getPadding() {
        return _padding;
    }

    @Override
    public void setPadding(String padding) {
        _padding = padding;
    }

    @Override
    public byte[] getIv() {
        return _iv;
    }

    @Override
    public void setIv(byte[] iv) {
        _iv = iv;
    }

    @Override
    public long getDataLength() {
        return _dataLength;
    }

    @Override
    public void setDataLength(long dataLength) {
        _dataLength = dataLength;
    }

    @Override
    public byte[] getDataHash() {
        return _dataHash;
    }

    @Override
    public void setDataHash(byte[] dataHash) {
        _dataHash = dataHash;
    }

    private String trimNullBytes(byte[] data) {
        int length = 0;
        while (length < data.length && data[length] != 0) {
            ++length;
        }

        return new String(data, 0, length, StandardCharsets.UTF_8);
    }

    private byte[] padNullBytes(String input) {
        byte[] result = new byte[16];
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        int length = Math.min(inputBytes.length, 16);
        System.arraycopy(inputBytes, 0, result, 0, length);

        return result;
    }

    private long bytesToLong(byte[] bytes) {
        if (bytes.length < 8) {
            throw new IllegalArgumentException("Массив должен содержать минимум 8 байтов.");
        }

        return ((long) bytes[0] << 56) | ((long) (bytes[1] & 0xFF) << 48) |
                ((long) (bytes[2] & 0xFF) << 40) | ((long) (bytes[3] & 0xFF) << 32) |
                ((long) (bytes[4] & 0xFF) << 24) | ((long) (bytes[5] & 0xFF) << 16) |
                ((long) (bytes[6] & 0xFF) << 8) | ((long) (bytes[7] & 0xFF));
    }

    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (value >> 56);
        bytes[1] = (byte) (value >> 48);
        bytes[2] = (byte) (value >> 40);
        bytes[3] = (byte) (value >> 32);
        bytes[4] = (byte) (value >> 24);
        bytes[5] = (byte) (value >> 16);
        bytes[6] = (byte) (value >> 8);
        bytes[7] = (byte) (value);
        return bytes;
    }
}
