package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Интерфейс получения и установки данных тегов для класса GetObjectsConfig
 * @author larin
 *
 */
public interface FindObjectsType extends Dto{
    /**
     * Получение данных тэга
     * @return
     */
    String getData();
    
    /**
     * Установка данных тэга
     * @param data
     */
    void setData(String data);
}
