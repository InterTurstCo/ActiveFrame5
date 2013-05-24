package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.net.URL;

/**
 * Служебный класс для загрузки файлов
 * @author vmatsukevich
 *         Date: 5/22/13
 *         Time: 1:46 PM
 */
public class FileUtils {

    /**
     * Возвращает файл в виде потока
     * @param relativePath относительный путь к файлу
     * @return файл в виде потока
     */
    public static InputStream getFileInputStream(String relativePath) {
        return FileUtils.class.getClassLoader().getResourceAsStream(relativePath);
    }

    /**
     * Возвращает {@link URL} файла
     * @param relativePath относительный путь к файлу
     * @return {@link URL} файла
     */
    public static URL getFileURL(String relativePath) {
        return FileUtils.class.getClassLoader().getResource(relativePath);
    }
}
