package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.dao.api.component.ServerComponentHandler;

/**
 * Интерфейс миграционного копмонента
 */
public interface Migrator extends ServerComponentHandler {
    /**
     * Выполняет действия миграционного компонента
     */
    void execute();
}
