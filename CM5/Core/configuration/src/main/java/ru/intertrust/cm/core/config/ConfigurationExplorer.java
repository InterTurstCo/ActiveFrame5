package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.Collection;
import java.util.List;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * Важно: методы интерфейса возвращают ссылки на непосредственно объекты конфигурации.
 * Изменение данных объектов недопустимо и напрямую приводит к некорректной работе приложения
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 1:28 PM
 */
public interface ConfigurationExplorer {

    String REFERENCE_TYPE_ANY = "*";

    /**
     * Возвращает конфигурацию
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @return конфигурация
     */
    Configuration getConfiguration();

    /**
     * Возвращает глобальные настройки конфигурации
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @return конфигурация
     */
    GlobalSettingsConfig getGlobalSettings();

    /**
     * Возвращает конфигурацию верхнего уровня
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param type класс конфигурации верхнего уровня
     * @param name имя конфигурации верхнего уровня
     * @return конфигурация верхнего уровня
     */
    <T> T getConfig(Class<T> type, String name);

    /**
     * Возвращает все конфигурации верхнего уровня данного типа type
     * Важно: метод возвращает ссылки на непосредственно объекты конфигурации.
     * Изменение данных объектов недопустимо и напрямую приводит к некорректной работе приложения
     * @param type класс конфигурации верхнего уровня
     * @return все конфигурации верхнего уровня данного типа type
     */
    <T> Collection<T> getConfigs(Class<T> type);

    /**
     * Возвращает конфигурацию типа доменного объекта
     * @param typeName имя типа доменного объекта
     * @return тип доменного объекта или null, если ничего не найдено
     */
    DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName);

    /**
     * Находит конфигурацию всех типов доменных объектов, являющихся дочерними для заданного типа.
     * Важно: метод возвращает ссылки на непосредственно объекты конфигурации.
     * Изменение данных объектов недопустимо и напрямую приводит к некорректной работе приложения
     * @param typeName имя типа доменного объекта
     * @param includeIndirect true, если в результат должны быть включены все уровни наследников
     * @return коллекция типов доменных объектов
     */
    Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля ()
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @param returnInheritedConfig указывает искать конфигруцию поля доменного объекта в иерархии типов
     *                                   доменных объектов или нет (искать только среди собственных конфигураций полей
     *                                   типа доменного объекта)
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig);

    /**
     * Находит конфигурацию отображаемого поля коллекции по имени представления коллекции (view) и имени поля в представлении коллекции
     * (collection-views.xml).
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param collectionViewName имя представления (view) коллекции
     * @param columnConfigName имя поля
     * @return конфигурацию отображаемого поля коллекции
     */
    CollectionColumnConfig getCollectionColumnConfig(String collectionViewName, String columnConfigName);

    /**
     * Поиск списка динамических групп по типу контекстного доменного объекта.
     * Важно: метод возвращает ссылки на непосредственно объекты конфигурации.
     * Изменение данных объектов недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObjectType типу контекстного объекта
     * @return спискок динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType);

    /**
     * Поиск динамических групп по отслеживаемым объектам.
     * Важно: метод возвращает ссылки на непосредственно объекты конфигурации.
     * Изменение данных объектов недопустимо и напрямую приводит к некорректной работе приложения
     * @param objectTypeName тип отслеживаемого объекта
     * @param status статус отслеживаемого объекта
     * @return список дескрипторов динамических групп
     */
    List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status);

    /**
     * Поиск конфигурации матрицы доступа для доменного объекта данного типа в данном статусе.
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObjectType тип доменного объекта
     * @param status статус доменного объекта
     * @return конфигурация матрицы доступа
     */
    AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status);

    /**
     * проверка того, что тип доменного обхекта - Attachment
     * @param domainObjectType тип доменного обхекта
     * @return true если тип доменного обхекта - Attachment
     */
    boolean isAttachmentType(String domainObjectType);

    boolean isReadPermittedToEverybody(String domainObjectType);

    /**
     * Получение матрицы доступа для типа
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObjectType тип доменного объекта
     * @return возвращает матрицу доступа
     */
    AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType);

    /**
     * Получение имени типа доменного объекта, который необходимо использовать при вычисление прав на доменный объект в случае
     * использования заимствования прав у связанного объекта
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param childTypeName имя типа, для которого необходимо вычислить тип объекта из которого заимствуются права
     * @return имя типа у которого заимствуются права или null в случае если заимствования нет
     */
    String getMatrixReferenceTypeName(String childTypeName);
    
    /**
     * Returns default toolbar for plugin.
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param pluginName componentName of plugin.
     * @return default toolbar of plugin. Can be NULL if toolbar not defined.
     */
    ToolBarConfig getDefaultToolbarConfig(String pluginName);

    /**
     * Возвращает конфигурацию логирования sql-запросов
     * Важно: метод возвращает ссылку на непосредственно объект конфигурации.
     * Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @return конфигурация логирования sql-запросов
     */
    SqlTrace getSqlTraceConfiguration();
    

    /**
     * Возвращает родительский тип доменного объекта
     * @param typeName имя типа доменного объекта
     * @return тип родительского объекта; null если такой отсутствует
     */
    String getDomainObjectParentType(String typeName);

    /**
     * Возвращает корневой тип доменного объекта
     * @param typeName имя типа доменного объекта
     * @return тип корневого объекта; текущий тип, если нет иерархии
     */
    String getDomainObjectRootType(String typeName);

    /**
     * Нахождение иерархии наследования по цепочке от корневого типа ДО до непосредственного родителя
     * @param typeName имя типа доменного объекта
     * @return цепочку от корня до родителя. Если нет родителя - пустой массив.
     */
    String[] getDomainObjectTypesHierarchy(String typeName);
}
