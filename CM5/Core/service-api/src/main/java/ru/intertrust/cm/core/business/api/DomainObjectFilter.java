package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Интерфейс предназначен для реализации классами, осуществляющими фильтрацию доменных объектов по какому-либо принципу.
 * Обычно такие классы подключаются через конфигурацию, например, для фильтрации доменных объектов, попадающих
 * в заданную область поиска. 
 * 
 * @author apirozhkov
 */
public interface DomainObjectFilter {

    /**
     * Определяет, удовлетворяет ли доменный объект условиям фильтра
     * 
     * @param object Проверяемый доменный объект
     * @return true елси объект проходит через фильтр
     */
    boolean filter(DomainObject object);
}
