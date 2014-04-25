package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;
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

    public static class SearchAreaDetailsConfig {

        private IndexedDomainObjectConfig objectConfig;
        private String areaName;
        private String targetObjectType;

        public IndexedDomainObjectConfig getObjectConfig() {
            return objectConfig;
        }

        public String getAreaName() {
            return areaName;
        }

        public String getTargetObjectType() {
            return targetObjectType;
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

    public List<SearchAreaDetailsConfig> findEffectiveConfigs(DomainObject object) {
        ArrayList<SearchAreaDetailsConfig> result = new ArrayList<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            processConfigList(object, area.getName(), null, area.getTargetObjects(), result);
        }
        return result;
    }

    private void processConfigList(DomainObject object, String areaName, String targetObjectType,
            Collection<? extends IndexedDomainObjectConfig> list, ArrayList<SearchAreaDetailsConfig> result) {
        for (IndexedDomainObjectConfig config : list) {
            if (config instanceof TargetDomainObjectConfig) {
                targetObjectType = config.getType();
            }
            if (object.getTypeName().equalsIgnoreCase(config.getType())) {
                DomainObjectFilter filter = createFilter(config);
                if (filter == null || filter.filter(object)) {
                    SearchAreaDetailsConfig details = new SearchAreaDetailsConfig();
                    details.objectConfig = config;
                    details.areaName = areaName;
                    details.targetObjectType = targetObjectType;
                    result.add(details);
                }
            }
            for (IndexedContentConfig contentConfig : config.getContentObjects()) {
                if (object.getTypeName().equalsIgnoreCase(contentConfig.getType())) {
                    SearchAreaDetailsConfig details = new SearchAreaDetailsConfig();
                    details.objectConfig = config;
                    details.areaName = areaName;
                    details.targetObjectType = targetObjectType;
                    result.add(details);
                }
            }
            processConfigList(object, areaName, targetObjectType, config.getLinkedObjects(), result);
        }
    }

    @SuppressWarnings("unchecked")
    private DomainObjectFilter createFilter(IndexedDomainObjectConfig config) {
        
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
        return null;    //*****
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

    public Set<FieldDataType> getFieldTypes(String name, Collection<String> areas) {
        Set<FieldDataType> types = new HashSet<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            if (areas.contains(area.getName())) {
                findFieldTypes(name, area.getTargetObjects(), types);
            }
        }
        return types;
    }

    private void findFieldTypes(String fieldName, Collection<? extends IndexedDomainObjectConfig> configs,
            Set<FieldDataType> types) {
        for (IndexedDomainObjectConfig config : configs) {
            for (IndexedFieldConfig field : config.getFields()) {
                if (fieldName.equalsIgnoreCase(field.getName())) {
                    types.add(getFieldType(field, config.getType()));
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
