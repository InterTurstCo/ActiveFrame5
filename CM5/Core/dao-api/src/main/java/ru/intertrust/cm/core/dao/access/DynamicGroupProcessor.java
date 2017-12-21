package ru.intertrust.cm.core.dao.access;

import java.util.Set;
import java.util.concurrent.Future;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис для асинхронного пересчета динамических групп
 * Требуется для ускорения процесса миграции, когда группы пересчитываются позже все сразу
 * @author larin
 *
 */
public interface DynamicGroupProcessor {
    
    Future<Void> calculateDynamicGroupAcync(Set<Id> groupIds);

    Future<Void> calculateGroupGroupAcync(Set<Id> groupIds);
}
