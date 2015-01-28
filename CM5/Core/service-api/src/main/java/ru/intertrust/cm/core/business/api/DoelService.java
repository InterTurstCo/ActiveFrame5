package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;

/**
 * Сервис вычисления значений Domain object expression language (DOEL)
 *
 * @author apirozhkov
 */
public interface DoelService {

    /**
     * Интерфейс для удалённых вызовов сервиса
     */
    public interface Remote extends DoelService {
    }

    /**
     * Вычисляет DOEL-выражение в контексте заданного доменного объекта.
     * Требует наличия прав на чтение всех используемых объектов.
     * 
     * @param expression DOEL-выражение
     * @param contextId идентификатор контекстного доменного объекта
     * @return список значений выражения; может быть пустым
     */
    public <T extends Value> List<T> evaluate(String expression, Id contextId);

    /**
     * Вычисляет DOEL-выражение в контексте заданного доменного объекта.
     * Этот метод может использоваться только в случаях, когда выражение должно возвращать единственный результат.
     * Требует наличия прав на чтение всех используемых объектов.
     * 
     * @param expression DOEL-выражение
     * @param contextId идентификатор контекстного доменного объекта
     * @param valueClass класс возвращаемого значения
     * @return результат вычисления выражения; может быть null
     * @throws ClassCastException если тип значения не соответствует запрошенному, либо если выражение
     *      возвращает несколько значений
     */
    public <T extends Value> T evaluate(String expression, Id contextId, Class<T> valueClass);
}
