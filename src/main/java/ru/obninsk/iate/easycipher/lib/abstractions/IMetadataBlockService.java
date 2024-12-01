package ru.obninsk.iate.easycipher.lib.abstractions;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface IMetadataBlockService {
    boolean read(Path path);

    boolean write(OutputStream stream);

    int getBlockLength();

    String getAlgorithm();

    void setAlgorithm(String algorithm);

    String getMode();

    void setMode(String mode);

    String getPadding();

    void setPadding(String padding);

    byte[] getIv();

    void setIv(byte[] iv);

    long getDataLength();

    void setDataLength(long dataLength);

    byte[] getDataHash();

    void setDataHash(byte[] dataHash);
}
