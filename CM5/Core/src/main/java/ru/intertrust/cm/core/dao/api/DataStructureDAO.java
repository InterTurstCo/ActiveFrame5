package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.config.BusinessObjectConfig;

/**
 * DAO для работы со структурой базы данных (создание таблиц, колонок, индексов и т.п.)
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public interface DataStructureDAO {

    /**
     * Создает таблицу по конфигурации бизнес-объекта
     * @param config конфигурация бизнес-объекта
     */
    void createTable(BusinessObjectConfig config);

    /**
     * Возвращает кол-во таблиц в базе данных
     * @return кол-во таблиц в базе данных
     */
    Integer countTables();

    /**
     * Создает сервисные таблицы, которые не задаются конфигурацией бизнес-объектов
     */
    void createServiceTables();
}
