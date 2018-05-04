package ru.intertrust.cm.core.business.api;

import java.util.Set;

/**
 * Интерфейс менеджера кластера.
 * @author larin
 *
 */
public interface ClusterManager {
    /**
     * Регистрирует роль в менеджере кластера
     * @param roleName имя роли
     * @param singleton должен ли сервер назначать эту роль только одной ноде
     */
    void regRole(String roleName, boolean singleton);
    
    /**
     * Проверяет имеент ли текущая нода роль с переданным именем
     * @param roleName
     * @return
     */
    boolean hasRole(String roleName);
    
    /**
     * Получение идентификатора текущей ноды
     * @return
     */
    String getNodeId();
    
    /**
     * Получение списка идентификатора нод, которые имеют переданную роль
     * @param roleName
     * @return
     */
    Set<String> getNodesWithRole(String roleName);
    
    /**
     * Возвращает список всех запущенных нод
     * @return
     */
    Set<String> getNodeIds();

    /**
     * Bозвращает является ли текущий сервер ведущим
     * В случае если базы нет, то метод должен возвращать true.
     * (исключительный режим работы, инициализация базы одновременно работающими серверами запрещена).
     * @return
     */
    boolean isMainServer();
}
