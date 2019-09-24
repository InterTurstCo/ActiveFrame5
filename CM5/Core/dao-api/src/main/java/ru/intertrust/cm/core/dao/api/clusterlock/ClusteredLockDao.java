package ru.intertrust.cm.core.dao.api.clusterlock;

import ru.intertrust.cm.core.business.api.dto.impl.ClusteredLockImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Сервис хранения данных для сервиса кластерной блокировки
 */
public interface ClusteredLockDao {
    /**
     * Инициализация сервиса. Создаются необходимые структуры в базе данных. Запускатся должно только на сервере менеджере кластера.
     */
    void init();

    ClusteredLockImpl create(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo);

    ClusteredLockImpl update(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo);

    ClusteredLockImpl find(String category, String name, boolean lock);

    Set<ClusteredLockImpl> findAll(String category);

    void delete(String category, String name);
}
