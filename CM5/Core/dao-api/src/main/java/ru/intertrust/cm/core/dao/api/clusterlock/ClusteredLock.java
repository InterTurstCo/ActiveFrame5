package ru.intertrust.cm.core.dao.api.clusterlock;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Экземпляр блокировки.
 * @author larin
 *
 */
public interface ClusteredLock {
    /**
     * Имя блокировки
     */
    String getName();

    /**
     * Категория блокировки
     */
    String getCategory();

    /**
     * Признак, что блокировка занята.
     */
    boolean isLocked();

    /**
     * Владелец блокировки. Пустая строка, если не заблокирован
     */
    String getOwner();

    /**
     * Время последнего захвата блокировки
     */
    Optional<Instant> getLockTime();

    /**
     * Интервал, после которого блокировка будет принудительно снята с момента
     * getLockTime()
     */
    Duration getAutoUnlockTimeout();

    /**
     * Метаданные, которые передал предыдущий владелец блокировки при вызове
     * unlock(String tag), либо пустая строка если запись о блокировке была
     * удалена за давностью. CMJ будет тут хранить дату модификации документа в
     * момент разблокировки.
     */
    String getTag();
}
