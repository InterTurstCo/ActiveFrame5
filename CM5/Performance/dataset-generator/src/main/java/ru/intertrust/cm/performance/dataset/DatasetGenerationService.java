package ru.intertrust.cm.performance.dataset;

/**
 * Интерфейсы для локального и удаленного вызовов сервиса генерации множества доменных объектов.
 * @author erentsov
 *
 */
public interface DatasetGenerationService {
    public interface Remote extends DatasetGenerationService{       
    }
    
    /**
     * 
     * @param command
     * @return
     */
    public String execute(byte[] command);
}
