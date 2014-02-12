package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.InputStream;

/**
 * Интерфейс адаптера filenet
 * @author larin
 *
 */
public interface FileNetAdapter {
    /**
     * Удаление по пути
     * @param path
     */
    void delete(String path) throws Exception;

    /**
     * Загрузка по пути
     * @param path
     * @return
     */
    InputStream load(String path) throws Exception;

    /**
     * Сохранение
     * @param saveContent
     * @return
     */
    String save(byte[] saveContent) throws Exception;
}
