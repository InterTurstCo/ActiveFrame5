package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Сервис для вычисления значений выражений на DOEL.
 * 
 * @author apirozhkov
 */
public interface DoelEvaluator {

    /**
     * Вычисляет DOEL-выражение в контексте заданного доменного объекта.
     * Требует наличия прав на чтение всех используемых объектов.
     * 
     * @param expression DOEL-выражение
     * @param sourceObjectId идентификатор исходного доменного объекта
     * @param accessToken маркер доступа
     * @return набор значений выражения
     */
    public <T extends Value> List<T> evaluate(DoelExpression expression, Id sourceObjectId, AccessToken accessToken);

}
