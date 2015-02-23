package ru.intertrust.cm.core.business.api;

/**
 * Интерфейс миграционного копмонента
 */
public interface Migrator {
    /**
     * Выполняет действия миграционного компонента
     */
    void execute();
}
