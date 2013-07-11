package ru.intertrust.cm.core.dao.access;

/**
 * Субъект доступа &mdash; процесс, являющийся частью ядра системы
 * 
 * @author apirozhkov
 */
public class SystemSubject implements Subject {

    private String processId;

    /**
     * Создаёт экземпляр объекта
     * 
     * @param processId Идентификатор процесса
     * @throws NullPointerException если processId == null
     */
    public SystemSubject(String processId) {
        processId.getClass();   // Just to throw NullPointerException
        this.processId = processId;
    }

    @Override
    public String getName() {
        return processId;
    }

}
