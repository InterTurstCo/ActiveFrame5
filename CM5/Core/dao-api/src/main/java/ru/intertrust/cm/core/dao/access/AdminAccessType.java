package ru.intertrust.cm.core.dao.access;

/**
 * Тип доступа &mdash; выполнение административной операции.
 * Используется для определения прав на изменение конфигурации, назначение ролей пользователям и т.п.
 * Требует включения пользователя в группу "Администраторы".
 * <p>Объекты класса не создаются, вместо этого используется единственный предварительно созданный объект
 * {@link #ADMIN}.
 * 
 * @author apirozhkov
 */
public class AdminAccessType implements AccessType {

    /**
     * Единственный экземпляр объекта
     */
    public static final AdminAccessType ADMIN = new AdminAccessType();

    // Создание экземпляров снаружи невозможно
    private AdminAccessType() {
    };

    private static final int hash = "Admin".hashCode();

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;     // singleton объект можно сравнивать и так
    }

}
