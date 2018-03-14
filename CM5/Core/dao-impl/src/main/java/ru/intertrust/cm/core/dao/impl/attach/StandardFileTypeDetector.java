package ru.intertrust.cm.core.dao.impl.attach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardFileTypeDetector implements FileTypeDetector {

    private static final Logger logger = LoggerFactory.getLogger(StandardFileTypeDetector.class);

    @Override
    public String detectMimeType(String path) {
        try {
            return Files.probeContentType(Paths.get(path));
        } catch (IOException e) {
            logger.error("Failed to detect content type for file " + path, e);
            return null;
        }
    }

}
