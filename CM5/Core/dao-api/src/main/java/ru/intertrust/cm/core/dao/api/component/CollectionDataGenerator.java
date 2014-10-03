package ru.intertrust.cm.core.dao.api.component;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;

/**
 * Интерфейс, который реализуют классы-генераторы коллекций. Классы-генераторы нужны для оптимизации получения
 * коллекций. Вместо одного SQL запроса, который указывается в конфигурации коллекции, можно получать данные из
 * нескольких SQL запросов и объединять их результаты.
 * @author atsvetkov
 */
public interface CollectionDataGenerator extends ServerComponentHandler {
    
    /**
     * Возвращает коллекцию объектов {@link IdentifiableObjectCollection}, отфильтрованную и упорядоченную согласно
     * порядку сортировки.
     * @param filters список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param sortOrder порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit);

    /**
     * Возвращает количество элементов заданной коллекции, отфильтрованной согласно списку фильтров
     * @param filterValues список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return число элементов заданной коллекции
     */
    int findCollectionCount(List<? extends Filter> filterValues);
}
