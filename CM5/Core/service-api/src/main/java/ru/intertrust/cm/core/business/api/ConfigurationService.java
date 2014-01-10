package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.Collection;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 9/12/13
 *         Time: 10:41 AM
 */
public interface ConfigurationService {

    public interface Remote extends ConfigurationService {
    }

    /**
     * Возвращает конфигурацию
     * @return конфигурация
     */
    Configuration getConfiguration();

    /**
     * Возвращает конфигурацию верхнего уровня
     * @param type класс конфигурации верхнего уровня
     * @param name имя конфигурации верхнего уровня
     * @return конфигурация верхнего уровня
     */
    <T> T getConfig(Class<T> type, String name);

    /**
     * Возвращает все конфигурации верхнего уровня данного типа type
     * @param type класс конфигурации верхнего уровня
     * @return все конфигурации верхнего уровня данного типа type
     */
    <T> Collection<T> getConfigs(Class<T> type);

    /**
     * Находит конфигурацию всех типов доменных объектов, являющихся дочерними для заданного типа.
     * @param typeName имя типа доменного объекта
     * @param includeIndirect true, если в результат должны быть включены все уровни наследников
     * @return коллекция типов доменных объектов
     */
    Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля ()
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @param returnInheritedConfig указывает искать конфигруцию поля доменного объекта в иерархии типов
     *                                   доменных объектов или нет (искать только среди собственных конфигураций полей
     *                                   типа доменного объекта)
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig);

    /**
     * Находит конфигурацию отображаемого поля коллекции по имени коллекции и имени поля
     * @param collectionConfigName имя коллекции
     * @param columnConfigName имя поля
     * @return конфигурацию отображаемого поля коллекции
     */
    CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName);

    /**
     * Поиск списка динамических групп по типу контекстного доменного объекта.
     * @param domainObjectType типу контекстного объекта
     * @return спискок динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType);

    /**
     * Поиск динамических групп по отслеживаемым объектам.
     * @param objectId идентификатор отслеживаемого объекта
     * @param status статус отслеживаемого объекта
     * @return список дескрипторов динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(Id objectId, String status);

    /**
     * Поиск конфигурации матрицы доступа для доменного объекта данного типа в данном статусе.
     * @param domainObjectType тип доменного объекта
     * @param status статус доменного объекта
     * @return конфигурация матрицы доступа
     */
    AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status);

    String getDomainObjectType(Id id);

    /**
     * проверка того, что тип доменного обхекта - Attachment
     * @param domainObjectType тип доменного обхекта
     * @return true если тип доменного обхекта - Attachment
     */
    boolean isAttachmentType(String domainObjectType);
}
