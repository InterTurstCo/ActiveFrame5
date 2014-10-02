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

    IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit);

}
