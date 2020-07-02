package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.simpledata.SimpleData;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleSearchOrder;

import java.util.List;

/**
 * Сервис хранения простых данных
 */
public interface SimpeDataStorage {

    public interface Remote extends SimpeDataStorage{
    }

    /**
     * Сохранить данные
     * @param data
     */
    void save(SimpleData data);

    /**
     * Найти данные по фильтру
     * @param filters Объединение фильтров по И
     * @return
     */
    List<SimpleData> find(String type, List<SimpleDataSearchFilter> filters, List<String> resultFields, List<SimpleSearchOrder> resultOrder);

    /**
     * Удалить данные найденные по фильтру
     * @param filters Объединение фильтров по И
     */
    void delete(String type, List<SimpleDataSearchFilter> filters);

    /**
     * Получение данных по ID
     * @param id
     * @return
     */
    SimpleData find(String type, String id);

    /**
     * Удаление данных по ID
     * @param id
     */
    void delete(String id);
}
