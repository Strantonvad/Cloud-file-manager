package ru.home.cloud.file.manager.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
    private final String fileName;
    private long size;
    private final LocalDateTime lastModified;

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);

            if (Files.isDirectory(path)) {
                this.size = -1L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));

        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
