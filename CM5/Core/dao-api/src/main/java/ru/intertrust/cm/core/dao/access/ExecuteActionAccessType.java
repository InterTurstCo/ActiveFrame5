package ru.intertrust.cm.core.dao.access;

/**
 * Тип доступа &mdash; выполнение заданного конфигурируемого действия.
 * @author atsvetkov
 */
public class ExecuteActionAccessType implements AccessType {

    private String actionName;

    /**
     * Создаёт экземпляр типа доступа
     * @param actionName название конфигурируемого действия
     */
    public ExecuteActionAccessType(String actionName) {
        actionName.getClass(); // Just to throw NullPointerException
        this.actionName = actionName;
    }

    /**
     * @return название конфигурируемого действия
     */
    public String getActionName() {
        return actionName;
    }

    @Override
    public int hashCode() {
        return actionName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !ExecuteActionAccessType.class.equals(obj.getClass())) {
            return false;
        }
        ExecuteActionAccessType other = (ExecuteActionAccessType) obj;
        return actionName.equals(other.actionName);
    }

}
