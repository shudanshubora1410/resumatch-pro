package com.resumatchpro.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private static final Logger log = LoggerFactory.getLogger(FileStorageUtil.class);

    @Value("${app.storage.local-path:./uploads/resumes/}")
    private String storagePath;

    /**
     * Store a file and return the stored filename (UUID-based).
     */
    public String store(InputStream inputStream, String originalFilename,
                         String fileType) throws IOException {
        String extension = fileType.equals("PDF") ? ".pdf" : ".docx";
        String storedName = UUID.randomUUID().toString() + extension;

        Path uploadDir = Paths.get(storagePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(storedName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored: {} -> {} ({} bytes)", originalFilename, storedName,
                Files.size(filePath));

        return storedName;
    }

    /**
     * Get the full file path for a stored file.
     */
    public Path getFilePath(String storedFilename) {
        return Paths.get(storagePath, storedFilename);
    }

    /**
     * Check if a stored file exists.
     */
    public boolean exists(String storedFilename) {
        return Files.exists(getFilePath(storedFilename));
    }

    /**
     * Delete a stored file.
     */
    public void delete(String storedFilename) {
        try {
            Path path = getFilePath(storedFilename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", storedFilename, e.getMessage());
        }
    }
}
