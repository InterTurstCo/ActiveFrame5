package ru.intertrust.cm.core.dao.impl.attach;

import org.apache.tika.Tika;

public class TikaFileTypeDetector implements FileTypeDetector {

    private final Tika engine = new Tika();

    @Override
    public String detectMimeType(String path) {
        return engine.detect(path);
    }

}
