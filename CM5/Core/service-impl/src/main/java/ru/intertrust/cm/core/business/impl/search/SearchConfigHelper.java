package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.doel.DoelValidator;
import ru.intertrust.cm.core.config.search.DomainObjectFilterConfig;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.SearchLanguageConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Класс, содержащий вспомогательные методы для облегчения работы с конфигурацией подсистемы поиска.
 * Экземпляр класса создаётся как bean в контексте приложения Spring и внедряется в использующие его классы
 * с помощью механизмов injection.
 * 
 * @author apirozhkov
 */
public class SearchConfigHelper {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    private List<SearchLanguageConfig> languageConfigs;

    /**
     * Класс, созданный для удобства работы с конфигурацией областей поиска.
     * Строится вокруг конфигурации одного доменного объекта внутри области поиска, но содержит также
     * информацию, полученную из вышестоящих элементов конфигурации, полезную для работы с этим доменным объектом.
     */
    public static class SearchAreaDetailsConfig {

        private IndexedDomainObjectConfig[] objectConfigChain;
        private String areaName;
        private String targetObjectType;

        SearchAreaDetailsConfig() {
        }

        SearchAreaDetailsConfig(List<IndexedDomainObjectConfig> objectConfigChain,
                String areaName, String targetObjectType) {
            this.objectConfigChain = objectConfigChain.toArray(new IndexedDomainObjectConfig[objectConfigChain.size()]);
            this.areaName = areaName;
            this.targetObjectType = targetObjectType;
        }

        /**
         * Возвращает конфигурацию одного доменного объекта внутри области поиска -
         * главный элемент, вокруг которого был созданный данный объект.
         * Возвращаемое значение не должно быть null.
         */
        public IndexedDomainObjectConfig getObjectConfig() {
            return objectConfigChain[0];
        }

        /**
         * Возвращает массив, содержащий цепочку включаемых элементов конфигурации снизу вверх (от частного к общему).
         * Первым элементом массива является исходный элемент конфигурации (тот же, что возвращается методом
         * {@link #getObjectConfig()}, последним - конфигурация содержащего его целевого объекта.
         * Это может быть один и тот же элемент, если исходный объект - {@link TargetDomainObjectConfig}.
         * Все элементы массива, кроме последнего, (если они есть) имеют тип {@link LinkedDomainObjectConfig}.
         */
        public IndexedDomainObjectConfig[] getObjectConfigChain() {
            return objectConfigChain;
        }

        /**
         * Возвращает имя области поиска, содержащей исходный элемент конфигурации.
         */
        public String getAreaName() {
            return areaName;
        }

        /**
         * Возвращает имя типа целевого доменного объекта, содержащего исходный элемент конфигурации.
         * @return
         */
        public String getTargetObjectType() {
            return targetObjectType;
        }

        @Override
        public int hashCode() {
            int hash = 58179;
            hash += objectConfigChain.hashCode();
            hash *= 31;
            hash += areaName.hashCode();
            hash *= 31;
            hash += targetObjectType.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SearchAreaDetailsConfig that = (SearchAreaDetailsConfig) obj;
            if (!this.areaName.equals(that.areaName) ||
                    !this.targetObjectType.equals(that.targetObjectType) ||
                    this.objectConfigChain.length != that.objectConfigChain.length) {
                return false;
            }
            for (int i = 0; i < objectConfigChain.length; i++) {
                if (!this.objectConfigChain[i].equals(that.objectConfigChain[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @PostConstruct
    private void initGlobalConfigs() {
        languageConfigs = configurationExplorer.getConfig(GlobalSettingsConfig.class, "").getSearchLanguages();
        /*if (languageConfigs == null) {
            languageConfigs = Collections.emptyList();
        }*/
    }

    /**
     * Возвращает все типы объектов в заданных областях поиска.
     * @param areaNames имена областей поиска
     * @param targetObjectType имя типа целевого объекта; может быть null
     * @return множество типов объектов
     */
    public Set<String> findAllObjectTypes(List<String> areaNames, String targetObjectType) {
        return findObjectTypesContainingField(null, areaNames, targetObjectType);
    }

    /**
     * Возвращает все типы объектов в областях поиска, содержащие поле с заданным именем.
     * @param field имя поля; null - любое поле
     * @param areaNames имена областей поиска
     * @param targetObjectType имя типа целевого объекта; может быть null
     * @return множество типов объектов (пустой, если поле не найдено)
     */
    public Set<String> findObjectTypesContainingField(String field, List<String> areaNames, String targetObjectType) {
        HashSet<String> configs = new HashSet<>();
        for (String area : areaNames) {
            SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
            for (TargetDomainObjectConfig targetConfig : areaConfig.getTargetObjects()) {
                if (targetObjectType == null || targetObjectType.equalsIgnoreCase(targetConfig.getType())) {
                    addAllObjectTypesContainingField(field, targetConfig, configs);
                }
            }
        }
        return configs;
    }

    private void addAllObjectTypesContainingField(String field, IndexedDomainObjectConfig root, Set<String> result) {
        for (IndexedFieldConfig fieldConfig : root.getFields()) {
            if (field == null || field.equalsIgnoreCase(fieldConfig.getName())) {
                result.add(root.getType());
                break;
            }
        }
        for (LinkedDomainObjectConfig linkedConfig : root.getLinkedObjects()) {
            addAllObjectTypesContainingField(field, linkedConfig, result);
        }
    }

    /**
     * Возвращает все типы объектов в областях поиска, содержащие вложения.
     * @param areaNames список имён областей поиска
     * @param targetObjectType имя типа целевого объекта; может быть null
     * @return множество типов объектов (пустой, если вложения не найдены)
     */
    public Set<String> findObjectTypesWithContent(List<String> areaNames, String targetObjectType) {
        return findObjectTypesWithContent(null, areaNames, targetObjectType);
    }

    /**
     * Возвращает все типы объектов в областях поиска, содержащие вложения заданного типа.
     * @param type имя типа вложений; null - любой тип
     * @param areaNames список имён областей поиска
     * @param targetObjectType имя типа целевого объекта; может быть null
     * @return множество типов объектов (пустой, если вложения не найдены)
     */
    public Set<String> findObjectTypesWithContent(String type, List<String> areaNames, String targetObjectType) {
        HashSet<String> configs = new HashSet<>();
        for (String area : areaNames) {
            SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
            for (TargetDomainObjectConfig targetConfig : areaConfig.getTargetObjects()) {
                if (targetObjectType == null || targetObjectType.equalsIgnoreCase(targetConfig.getType())) {
                    addAllObjectTypesWithContent(type, targetConfig, configs);
                }
            }
        }
        return configs;
    }

    private void addAllObjectTypesWithContent(String type, IndexedDomainObjectConfig root, Set<String> result) {
        for (IndexedContentConfig contentConfig : root.getContentObjects()) {
            if (type == null || type.equalsIgnoreCase(contentConfig.getType())) {
                result.add(contentConfig.getType());
                break;
            }
        }
        for (LinkedDomainObjectConfig linkedConfig : root.getLinkedObjects()) {
            addAllObjectTypesWithContent(type, linkedConfig, result);
        }
    }

    /**
     * Возвращает все конфигурации полей с заданным именем в заданной области поиска.
     * Поля с одинаковым именем могут присутствовать в конфигурации области поиска у разных целевых объектов.
     * 
     * @param field имя поля
     * @param area имя области поиска
     * @return список конфигураций (пустой, если поле не найдено в этой области)
     * @throws IllegalArgumentException если область поиска с заданным именем отсутствует в конфигурации
     */
    public List<IndexedFieldConfig> findIndexedFieldConfigs(String field, String area) {
        SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
        if (areaConfig == null) {
            throw new IllegalArgumentException("Search area '" + area + "' does not exist");
        }
        ArrayList<IndexedFieldConfig> result = new ArrayList<>();
        for (TargetDomainObjectConfig objectConfig : areaConfig.getTargetObjects()) {
            IndexedFieldConfig fieldConfig = findIndexedFieldConfig(field, objectConfig);
            if (fieldConfig != null) {
                result.add(fieldConfig);
            }
        }
        return result;
    }

    /**
     * Возвращает конфигурацию поля с заданным именем у заданного целевого объекта в заданной области поиска.
     * Уникальность поля в заданных условиях должен обеспечивать компонент валидации конфигурации.
     * 
     * @param field имя поля
     * @param area имя области поиска
     * @param targetType имя типа целевого объекта
     * @return конфигурация поля или null, если поле не найдено
     * @throws IllegalArgumentException если область поиска с заданным именем отсутствует в конфигурации
     *          или не содержит заданный целевой объект
     */
    public IndexedFieldConfig findIndexedFieldConfig(String field, String area, String targetType) {
        SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
        if (areaConfig == null) {
            throw new IllegalArgumentException("Search area '" + area + "' does not exist");
        }
        for (TargetDomainObjectConfig objectConfig : areaConfig.getTargetObjects()) {
            if (targetType.equalsIgnoreCase(objectConfig.getType())) {
                return findIndexedFieldConfig(field, objectConfig);
            }
        }
        throw new IllegalArgumentException("Target type '" + targetType + "' not found in search area '" + area + "'");
    }

    private IndexedFieldConfig findIndexedFieldConfig(String field, IndexedDomainObjectConfig objectConfig) {
        for (IndexedFieldConfig fieldConfig : objectConfig.getFields()) {
            if (field.equalsIgnoreCase(fieldConfig.getName())) {
                return fieldConfig;
            }
        }
        for (LinkedDomainObjectConfig linkedConfig : objectConfig.getLinkedObjects()) {
            IndexedFieldConfig result = findIndexedFieldConfig(field, linkedConfig);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Возвращает элементы конфигураци областей поиска, соответствующие заданному типу объекта.
     * 
     * @param objectType имя типа объекта
     * @return список элементов конфигурации (пустой, если заданный тип не сконфигурирован для поиска)
     */
    public List<SearchAreaDetailsConfig> findEffectiveConfigs(String objectType) {
        ArrayList<SearchAreaDetailsConfig> result = new ArrayList<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            processConfigList(objectType, area.getName(), null, area.getTargetObjects(),
                    new LinkedList<IndexedDomainObjectConfig>(), result);
        }
        return result;
    }

    private void processConfigList(String objectType, String areaName, String targetObjectType,
            Collection<? extends IndexedDomainObjectConfig> list, LinkedList<IndexedDomainObjectConfig> parents,
            ArrayList<SearchAreaDetailsConfig> result) {
        for (IndexedDomainObjectConfig config : list) {
            if (config instanceof TargetDomainObjectConfig) {
                targetObjectType = config.getType();
            }
            parents.addFirst(config);
            //if (object.getTypeName().equalsIgnoreCase(config.getType())) {
            if (isSuitableType(config.getType(), objectType)) {
                /*DomainObjectFilter filter = createFilter(config);
                if (filter == null || filter.filter(object)) {*/
                    result.add(new SearchAreaDetailsConfig(parents, areaName, targetObjectType));
                //}
            }
            for (IndexedContentConfig contentConfig : config.getContentObjects()) {
                //if (object.getTypeName().equalsIgnoreCase(contentConfig.getType())) {
                if (isSuitableType(contentConfig.getType(), objectType)) {
                    result.add(new SearchAreaDetailsConfig(parents, areaName, targetObjectType));
                }
            }
            processConfigList(objectType, areaName, targetObjectType, config.getLinkedObjects(), parents, result);
            parents.removeFirst();
        }
    }

    /**
     * Определяет, подходит ли действительный тип объекта к типу, описанному в конфигурации (с учётом наследования).
     * Тип считается подходящим, если он совпадает с требуемым или является его наследником (в т.ч. косвенным).
     * 
     * @param neededType имя требуемого типа
     * @param realType имя действительного типа
     * @return true, если действительный тип объекта подходит к требуемому
     */
    public boolean isSuitableType(String neededType, String realType) {
        do {
            if (realType.equalsIgnoreCase(neededType)) {
                return true;
            }
            realType = configurationExplorer.getDomainObjectParentType(realType);
        } while(realType != null);
        return false;
    }

    /**
     * Создаёт фильтр, описанный в конфигурации доменного объекта в области поиска.
     * 
     * @param config конфигурация доменного объекта в области поиска
     * @return объект фильтра или null, если фильтр не определён в конфигурации
     */
    public DomainObjectFilter createFilter(IndexedDomainObjectConfig config) {
        ApplicationContext ctx = SpringApplicationContext.getContext();

        DomainObjectFilterConfig filterConfig = config.getFilter();
        if (filterConfig == null) {
            return null;
        }
        if (filterConfig.getJavaClass() != null) {
            JavaClassDomainObjectFilter javaClassDomainObjectFilter =
                    (JavaClassDomainObjectFilter) ctx.getBean(JavaClassDomainObjectFilter.class);
            javaClassDomainObjectFilter.setJavaClass(filterConfig.getJavaClass());
            return javaClassDomainObjectFilter;
        } else if (filterConfig.getSearchQuery() != null) {
            SqlQueryDomainObjectFilter sqlQueryDomainObjectFilter =
                    (SqlQueryDomainObjectFilter) ctx.getBean(SqlQueryDomainObjectFilter.class);
            sqlQueryDomainObjectFilter.setSqlQuery(filterConfig.getSearchQuery());
            return sqlQueryDomainObjectFilter;

        } else if (filterConfig.getConditionsScript() != null) {
            ConditionsScriptDomainObjectFilter conditionsScriptDomainObjectFilter =
                    (ConditionsScriptDomainObjectFilter) ctx.getBean(ConditionsScriptDomainObjectFilter.class);
            conditionsScriptDomainObjectFilter.setConditionsScript(filterConfig.getConditionsScript());
            return conditionsScriptDomainObjectFilter;

        }
        throw new IllegalStateException("Wrong filter configuration");
    }

    public static class FieldDataType {
        private FieldType dataType;
        private boolean multivalued = false;

        FieldDataType(FieldType dataType) {
            this.dataType = dataType;
        }

        FieldDataType(FieldType dataType, boolean multivalued) {
            this.dataType = dataType;
            this.multivalued = multivalued;
        }

        public FieldType getDataType() {
            return dataType;
        }

        public boolean isMultivalued() {
            return multivalued;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !FieldDataType.class.equals(obj.getClass())) {
                return false;
            }
            FieldDataType that = (FieldDataType) obj;
            return this.dataType == that.dataType && this.multivalued == that.multivalued;
        }

        @Override
        public int hashCode() {
            return 529 * dataType.hashCode() + (multivalued ? 31 : 887);
        }
    }

    /**
     * Определяет тип данных индексируемого поля, определённого в конфигурации области поиска.
     * 
     * @param config конфигурация индексируемого поля
     * @param objectType имя типа объекта, содержащего поле
     * @return тип данных, хранимых в индексируемом поле
     * @throws IllegalArgumentException если конфигурация ссылается на несуществующее поле
     */
    public FieldDataType getFieldType(IndexedFieldConfig config, String objectType) {
        if (config.getDoel() != null) {
            DoelExpression expr = DoelExpression.parse(config.getDoel());
            DoelValidator.DoelTypes analyzed = DoelValidator.validateTypes(expr, objectType);
            if (!analyzed.isCorrect()) {
                //TODO Report error message
                return null;
            }
            Set<FieldType> types = analyzed.getResultTypes();
            return new FieldDataType(types.size() == 1 ? types.iterator().next() : FieldType.STRING,
                    !analyzed.isSingleResult());
        } else {
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(objectType.toLowerCase(),
                    config.getName().toLowerCase());
            if (fieldConfig == null) {
                throw new IllegalArgumentException(config.getName() + " isn't defined in type " + objectType);
            }
            return new FieldDataType(fieldConfig.getFieldType());
        }
    }

    /**
     * 
     * @param config конфигурация индексируемого поля
     * @param objectType имя типа объекта, содержащего поле
     * @param value значение вычисляемого поля. По значению определяется тип вычисляемого поля.
     * @return тип данных, хранимых в индексируемом поле
     */
    public FieldDataType getFieldType(IndexedFieldConfig config, String objectType, Object value) {
        if (config.getScript() != null) {
            return new FieldDataType(getValueType(value));
        } else {
            return getFieldType(config, objectType);
        }
    }

    private FieldType getValueType(Object value) {
        if(value instanceof Date){
            return FieldType.DATETIME;            
        }else if(value instanceof Long){
            return FieldType.LONG;  
        }else if(value instanceof Number){
            return FieldType.DECIMAL;  
        }else{
            return FieldType.STRING;
        }        
    }

    public Set<SearchFieldType> getFieldTypes(String name, Collection<String> areas) {
        Set<SearchFieldType> types = new HashSet<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            if (areas.contains(area.getName())) {
                findFieldTypes(name, area.getTargetObjects(), types);
            }
        }
        return types;
    }

    private void findFieldTypes(String fieldName, Collection<? extends IndexedDomainObjectConfig> configs,
            Set<SearchFieldType> types) {
        for (IndexedDomainObjectConfig config : configs) {
            for (IndexedFieldConfig field : config.getFields()) {
                if (fieldName.equalsIgnoreCase(field.getName())) {
                    FieldDataType type = getFieldType(field, config.getType());
                    types.add(SearchFieldType.getFieldType(type.getDataType(), type.isMultivalued()));
                    break;      // No more fields with this name should be in this object config
                }
            }
            findFieldTypes(fieldName, config.getLinkedObjects(), types);
        }
    }

    public boolean isAttachmentObject(DomainObject object) {
        String type = object.getTypeName();
        return configurationExplorer.isAttachmentType(type);
    }

    /**
     * Определяет имя поля в объекте вложения, хранящего ссылку на родительский объект.
     * Необходимость этого метода обусловлена особенностью реализации объектов вложений, которые объявляются
     * в конфигурации отличным от других доменных объектов способом. Объекты вложений имеют ряд полей 
     * с фиксированными именами, но поле связи с родительским объектом индивидуально для каждого
     * типа вложения.
     * 
     * @param attachmentType имя типа объекта вложения
     * @param parentType имя типа индексируемого объекта, включающего данное вложение
     * @return имя поля, содержащего связь. Может использоваться в вызове {@link DomainObject#getReference(String)}
     * @throws IllegalArgumentException если вложение никак не связано с родительским объектом,
     *          или если какой-либо из типов не определён в конфигурации
     */
    public String getAttachmentParentLinkName(String attachmentType, String parentType) {
        while (parentType != null) {
            DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, parentType);
            if (config == null) {
                throw new IllegalArgumentException(parentType + " is not defined in configuration");
            }
            AttachmentTypesConfig attachmentsConfig = config.getAttachmentTypesConfig();
            if (attachmentsConfig != null) {
                for (AttachmentTypeConfig attachmentConfig : attachmentsConfig.getAttachmentTypeConfigs()) {
                    if (attachmentType.equalsIgnoreCase(attachmentConfig.getName())) {
                        ReferenceFieldConfig parentFieldConfig = attachmentConfig.getParentReference();
                        if (parentFieldConfig != null) {
                            return parentFieldConfig.getName();
                        } else {
                            return parentType;
                        }
                    }
                }
            }
            parentType = config.getExtendsAttribute();
        }
        throw new IllegalArgumentException(parentType + " type does not have attachments of type " + attachmentType);
    }

    public DomainObjectTypeConfig getTargetObjectType(String collectionName) {
        CollectionConfig collConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        return configurationExplorer.getConfig(DomainObjectTypeConfig.class, collConfig.getName()); //*****
    }

    /**
     * Возвращает список идентификаторов языков, сконфигурированных для поиска глобально.
     * Если языки не сконфигурированы, возвращает список с единтсвенным элементом, содержащим пустую строку.
     * 
     * @return Список строк - идентификаторов языков
     */
    public List<String> getSupportedLanguages() {
        if (languageConfigs == null || languageConfigs.size() == 0) {
            return Collections.singletonList("");
        }
        ArrayList<String> result = new ArrayList<>(languageConfigs.size());
        for (SearchLanguageConfig config : languageConfigs) {
            result.add(config.getLangId().trim());
        }
        return result;
    }

    /**
     * Возвращает список идентификаторов языков, сконфигурированных для поиска в конкретном поле.
     * Если поле сконфигурировано для безъязыкового поиска (language=""), список будет содержать пустую строку.
     * Если поле не найдено в конфигурации заданной области поиска, вернётся пустой список.
     * 
     * @param field Имя индексированного поля
     * @param area Имя области поиска
     * @return Список строк - идентификаторов языков
     */
    public List<String> getSupportedLanguages(String field, String area) {
        List<IndexedFieldConfig> foundFields = findIndexedFieldConfigs(field, area);
        if (foundFields.size() == 0) {
            return Collections.emptyList();
        }
        HashSet<String> langIds = new HashSet<>();
        boolean defaultAdded = false;
        for (IndexedFieldConfig fieldConfig : foundFields) {
            String lang = fieldConfig.getLanguage();
            if (lang == null) {
                if (!defaultAdded) {
                    langIds.addAll(getSupportedLanguages());
                    defaultAdded = true;
                }
                continue;
            }
            String[] langs = lang.split("[\\s,:;]+");
            if (langs.length == 1 && langs[0].trim().length() == 0) {
                langIds.add("");
                continue;
            }
            for (String langId : langs) {
                if (langId.trim().length() > 0) {
                    langIds.add(langId.trim());
                }
            }
        }
        return new ArrayList<>(langIds);
    }
}
