package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Класс временного файла. Файл удаляется, когда закрывается поток
 * @author larin
 *
 */
public class TempFileInputStream extends FileInputStream {
    private File file;

    public TempFileInputStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    public TempFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (file != null) {
                file.delete();
                file = null;
            }
        }
    }
}
