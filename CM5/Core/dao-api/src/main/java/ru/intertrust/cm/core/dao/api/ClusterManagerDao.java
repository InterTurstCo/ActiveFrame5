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
    String getNodeId();

    /**
     * Получение идентификатора кластера
     * @return
     */
    String getClusterId();

    /**
     * Получение имени узла кластера
     * @return
     */
    String getNodeName();

    /**
     * Проверка является ли узел с переданным идентификатором активным узлом текущего кластера.
     * @param nodeId
     * @return
     */
    boolean hasNode(String nodeId);
}
