package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Общий интерфейс фильтров области поиска (фильтров, реализованных в Java классе, в виде SQL запроса, в виде скрипта).
 * Создан для удобства вызова фильтров разного типа.
 * @author atsvetkov
 */

public interface DomainObjectFilter {

    /**
     * Определяет, удовлетворяет ли доменный объект условиям фильтра
     * 
     * @param object Проверяемый доменный объект
     * @return true если объект проходит через фильтр
     */
    boolean filter(DomainObject object);
}
