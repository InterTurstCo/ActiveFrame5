package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.simpledata.SimpleData;
import ru.intertrust.cm.core.business.api.simpledata.SimpleSearchOrder;

import java.util.List;
import java.util.Map;

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
    void store(SimpleData data);

    /**
     * Найти данные
     * @param filter значение полей, которые объеденяются по И. Имя типа данных так же можно передать в
     *               фильтре в поле с именем type
     * @return
     */
    List<SimpleData> find(Map<String, Value> filter, List<String> resultFields, List<SimpleSearchOrder> resultOrder);

    /**
     * Удалить данные
     * @param filter значение полей, которые объеденяются по И для поиска удаляемых значений.
     *               Имя типа данных так же можно передать в фильтре в поле с именем type
     */
    void delete(Map<String, Value> filter);
}
