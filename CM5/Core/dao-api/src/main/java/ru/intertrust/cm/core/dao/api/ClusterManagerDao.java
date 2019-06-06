package ru.intertrust.cm.core.dao.api;

/**
 * Менеджер кластера, слой DAO
 * @author larin
 *
 */
public interface ClusterManagerDao {
    
    /**
     * Получение идентификатора узла кластера
     * @return
     */
    public String getNodeId();
    
    /**
     * Получение имени узла кластера
     * @return
     */
    public String getNodeName();
}
