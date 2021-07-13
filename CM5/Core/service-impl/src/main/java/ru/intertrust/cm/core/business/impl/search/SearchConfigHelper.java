package ru.intertrust.cm.core.business.impl.search;

import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.Trio;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.search.CompoundFieldConfig;
import ru.intertrust.cm.core.config.search.DomainObjectFilterConfig;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldScriptConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.SearchLanguageConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.dao.doel.DoelValidator;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Класс, содержащий вспомогательные методы для облегчения работы с конфигурацией подсистемы поиска.
 * Экземпляр класса создаётся как bean в контексте приложения Spring и внедряется в использующие его классы
 * с помощью механизмов injection.
 * 
 * @author apirozhkov
 */
public class SearchConfigHelper implements ApplicationListener<ConfigurationUpdateEvent> {

    public static final String ALL_TYPES = "*";

    private static final Logger logger = LoggerFactory.getLogger(SearchConfigHelper.class);

    private static final Map<IndexedFieldScriptConfig.ScriptReturnType, SimpleSearchFieldType.Type> searchFieldTypes = new HashMap<>(4);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    private List<SearchLanguageConfig> languageConfigs;

    /**
     * Флаг отключения агента индексирования. Аггент полезно отключать в процессе импорта для ускорения процесса.
     */
    @org.springframework.beans.factory.annotation.Value("${index.agent.disable:false}")
    private boolean disableIndexing = false;

    private Map<String, ArrayList<SearchAreaDetailsConfig>> effectiveConfigsMap =
            Collections.synchronizedMap(new HashMap<String, ArrayList<SearchAreaDetailsConfig>>());

    private final Map<Trio<IndexedFieldConfig, CompoundFieldConfig, String>, Set<SearchFieldType>> fieldTypeMap = new ConcurrentHashMap<>();

    private Map<Pair<String, String>, String> attachmentParentLinkNameMap =
            Collections.synchronizedMap(new HashMap<Pair<String, String>, String>());

    private Map<Pair<String, String>, List<IndexedFieldConfig>> indexedFieldConfigsMap =
            Collections.synchronizedMap(new HashMap<Pair<String, String>, List<IndexedFieldConfig>>());

    private Map<Pair<String, String>, List<String>> supportedLanguagesMap =
            Collections.synchronizedMap(new HashMap<Pair<String, String>, List<String>>());

    private Map<Trio<String, Collection<String>, Collection<String>>, Set<SearchFieldType>> fieldTypesMap =
            Collections.synchronizedMap(new HashMap<Trio<String, Collection<String>, Collection<String>>, Set<SearchFieldType>>());

    private Map<Trio<String, List<String>, String>, Set<String>> objectTypesContainingFieldMap =
            Collections.synchronizedMap(new HashMap<Trio<String, List<String>, String>, Set<String>>());

    private Map<Trio<String, List<String>, String>, Set<String>> objectTypesWithContentMap =
            Collections.synchronizedMap(new HashMap<Trio<String, List<String>, String>, Set<String>>());

    private Map<Trio<String, String, String>, IndexedFieldConfig> indexedFieldConfigMap =
            Collections.synchronizedMap(new HashMap<Trio<String, String, String>, IndexedFieldConfig>());

    private Map<Trio<String, List<String>, String>, Collection<String>> applicableTypesMap =
            Collections.synchronizedMap(new HashMap<Trio<String, List<String>, String>, Collection<String>>());

    private List<String> supportedLanguages = null;

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
        clearCache();
        logger.info("Config is changed. Cache is cleaned");
    }

    /**
     * Класс, созданный для удобства работы с конфигурацией областей поиска.
     * Строится вокруг конфигурации одного доменного объекта внутри области поиска, но содержит также
     * информацию, полученную из вышестоящих элементов конфигурации, полезную для работы с этим доменным объектом.
     */
    public static class SearchAreaDetailsConfig {

        private IndexedDomainObjectConfig[] objectConfigChain;
        private String areaName;
        private String targetObjectType;
        private String solrServerKey;

        SearchAreaDetailsConfig() {
        }

        SearchAreaDetailsConfig(List<IndexedDomainObjectConfig> objectConfigChain,
                String areaName, String targetObjectType, String solrServerKey) {
            this.objectConfigChain = objectConfigChain.toArray(new IndexedDomainObjectConfig[objectConfigChain.size()]);
            this.areaName = areaName;
            this.targetObjectType = targetObjectType;
            this.solrServerKey = solrServerKey;
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

        public String getSolrServerKey() {
            return solrServerKey;
        }

        @Override
        public int hashCode() {
            int hash = 58179;
            hash += Arrays.hashCode(objectConfigChain);
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

    public SearchAreaConfig getSearchAreaDetailsConfig(String areaName) {
        return configurationExplorer.getConfig(SearchAreaConfig.class, areaName);
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
    protected Set<String> findAllObjectTypes(List<String> areaNames, String targetObjectType) {
        return findObjectTypesContainingField(null, areaNames, targetObjectType);
    }

    /**
     * Возвращает все типы объектов в областях поиска, содержащие поле с заданным именем.
     * @param field имя поля; null - любое поле
     * @param areaNames имена областей поиска
     * @param targetObjectType имя типа целевого объекта; может быть null
     * @return множество типов объектов (пустой, если поле не найдено)
     */
    private Set<String> findObjectTypesContainingField(String field, List<String> areaNames, String targetObjectType) {
        Trio<String, List<String>, String> key = new Trio<>(field, areaNames, targetObjectType);

        Set<String> result = objectTypesContainingFieldMap.get(key);
        if (result != null) {
            return result;
        }

        result = new HashSet<>();
        for (String area : areaNames) {
            SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
            for (TargetDomainObjectConfig targetConfig : areaConfig.getTargetObjects()) {
                if (targetObjectType == null || targetObjectType.equalsIgnoreCase(targetConfig.getType())) {
                    addAllObjectTypesContainingField(field, targetConfig, result);
                }
            }
        }

        objectTypesContainingFieldMap.put(key, result);
        return result;
    }

    private void addAllObjectTypesContainingField(String field, IndexedDomainObjectConfig root, Set<String> result) {
        String type = findType(field, root);
        if (type != null) {
            result.add(type);
        }

        boolean hasInNested = root.getLinkedObjects().stream()
                .filter(LinkedDomainObjectConfig::isNested)
                .anyMatch(it -> findType(field, it) != null);
        if (hasInNested) {
            result.add(root.getType());
        }

        root.getLinkedObjects().stream()
                .filter(linkedConfig -> !linkedConfig.isNested())
                .forEach(linkedConfig -> addAllObjectTypesContainingField(field, linkedConfig, result));
    }

    private String findType(String field, IndexedDomainObjectConfig root) {
        String type = null;
        boolean inRoot = root.getFields()
                .stream()
                .anyMatch(fieldConfig -> field == null || field.equalsIgnoreCase(fieldConfig.getName()));
        if (inRoot) {
            type = root.getType();
        }
        return type;
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
        Trio<String, List<String>, String> key = new Trio<>(type, areaNames, targetObjectType);

        Set<String> result = objectTypesWithContentMap.get(key);
        if (result != null) {
            return result;
        }

        result = new HashSet<>();
        for (String area : areaNames) {
            SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
            for (TargetDomainObjectConfig targetConfig : areaConfig.getTargetObjects()) {
                if (targetObjectType == null || targetObjectType.equalsIgnoreCase(targetConfig.getType())) {
                    addAllObjectTypesWithContent(type, targetConfig, result);
                }
            }
        }

        objectTypesWithContentMap.put(key, result);
        return result;
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
        Pair<String, String> key = new Pair<>(field, area);

        List<IndexedFieldConfig> result = indexedFieldConfigsMap.get(key);
        if (result != null) {
            return result;
        }


        SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
        if (areaConfig == null) {
            throw new IllegalArgumentException("Search area '" + area + "' does not exist");
        }

        result = new ArrayList<>();
        for (TargetDomainObjectConfig objectConfig : areaConfig.getTargetObjects()) {
            IndexedFieldConfig fieldConfig = findIndexedFieldConfig(field, objectConfig);
            if (fieldConfig != null) {
                result.add(fieldConfig);
            }
        }

        indexedFieldConfigsMap.put(key, result);
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
        Trio<String, String, String> key = new Trio<>(field, area, targetType);

        IndexedFieldConfig result = indexedFieldConfigMap.get(key);
        if (result != null) {
            return result;
        }

        SearchAreaConfig areaConfig = configurationExplorer.getConfig(SearchAreaConfig.class, area);
        if (areaConfig == null) {
            throw new IllegalArgumentException("Search area '" + area + "' does not exist");
        }
        for (TargetDomainObjectConfig objectConfig : areaConfig.getTargetObjects()) {
            if (targetType.equalsIgnoreCase(objectConfig.getType())) {
                result = findIndexedFieldConfig(field, objectConfig);
                indexedFieldConfigMap.put(key, result);
                return result;
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
        ArrayList<SearchAreaDetailsConfig> result = effectiveConfigsMap.get(objectType);
        if (result != null) {
            return result;
        }

        result = new ArrayList<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            processConfigList(objectType, area.getName(), null, area.getSolrServerKey(),
                    area.getTargetObjects(), new LinkedList<IndexedDomainObjectConfig>(), result);
        }

        effectiveConfigsMap.put(objectType, result);
        return result;
    }

    private void processConfigList(String objectType, String areaName, String targetObjectType, String solrServerKey,
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
                    result.add(new SearchAreaDetailsConfig(parents, areaName, targetObjectType, solrServerKey));
                //}
            }
            for (IndexedContentConfig contentConfig : config.getContentObjects()) {
                //if (object.getTypeName().equalsIgnoreCase(contentConfig.getType())) {
                if (isSuitableType(contentConfig.getType(), objectType)) {
                    result.add(new SearchAreaDetailsConfig(parents, areaName, targetObjectType, solrServerKey));
                }
            }
            processConfigList(objectType, areaName, targetObjectType, solrServerKey, config.getLinkedObjects(), parents, result);
            parents.removeFirst();
        }
    }

    public List<SearchAreaDetailsConfig> findChildConfigs(SearchAreaDetailsConfig config, boolean nested) {
        List<LinkedDomainObjectConfig> linkedObjects = config.getObjectConfig().getLinkedObjects();

        List<SearchAreaDetailsConfig> result = new ArrayList<>(linkedObjects.size());
        for (LinkedDomainObjectConfig child : linkedObjects) {
            if (child.isNested() ^ nested) {
                continue;
            }
            SearchAreaDetailsConfig details = new SearchAreaDetailsConfig();
            details.objectConfigChain = new IndexedDomainObjectConfig[config.getObjectConfigChain().length + 1];
            details.objectConfigChain[0] = child;
            for (int i = 0; i < config.getObjectConfigChain().length; ++i) {
                details.objectConfigChain[i + 1] = config.getObjectConfigChain()[i];
            }
            details.areaName = config.areaName;
            details.targetObjectType = config.targetObjectType;
            result.add(details);
        }
        return result;
    }

    public List<SearchAreaDetailsConfig> findChildConfigs(SearchAreaDetailsConfig config) {
        return findChildConfigs(config, false);
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

    /**
     * Определяет тип данных индексируемого поля, определённого в конфигурации области поиска.
     *
     * @param config     конфигурация индексируемого поля
     * @param objectType имя типа объекта, содержащего поле
     * @return тип данных, хранимых в индексируемом поле
     * @throws IllegalArgumentException если конфигурация ссылается на несуществующее поле
     */
    public Set<SearchFieldType> getFieldTypes(IndexedFieldConfig config, String objectType) {
        return getFieldTypes(config, null, objectType);
    }

    public Set<SearchFieldType> getFieldTypes(IndexedFieldConfig config, CompoundFieldConfig compoundFieldConfig, String objectType) {
        Trio<IndexedFieldConfig, CompoundFieldConfig, String> key = new Trio<>(config, compoundFieldConfig, objectType);

        Set<SearchFieldType> result = fieldTypeMap.get(new Trio<>(config, compoundFieldConfig, objectType));
        if (result != null) {
            return result;
        }

        if (config.getSolrPrefix() != null) {
            result = Collections.<SearchFieldType>singleton(new CustomSearchFieldType(config.getSolrPrefix()));
        } else if (getDoel(config, compoundFieldConfig) != null) {
            String doel = getDoel(config, compoundFieldConfig);
            DoelExpression expr = DoelExpression.parse(doel);
            DoelValidator.DoelTypes analyzed = DoelValidator.validateTypes(expr, objectType);
            if (!analyzed.isCorrect()) {
                logger.warn("DOEL expression for indexed field " + config.getName() + " in object " + objectType
                        + " [" + doel + "] is invalid");
                return null;
            }
            result = new HashSet<>(analyzed.getResultTypes().size() * 2);   // Considering default load factor (0.75)
            for (FieldType type : analyzed.getResultTypes()) {
                SimpleSearchFieldType.Type dataType = SimpleSearchFieldType.byFieldType(type);
                if (dataType != null) {
                    result.add(new SimpleSearchFieldType(dataType, !analyzed.isSingleResult()));
                } else {
                    result.add(new TextSearchFieldType(getSupportedLanguages(config), !analyzed.isSingleResult(),
                            config.getSearchBy()));
                }
            }
        } else {
            Set<SearchFieldType> scriptType = getScriptType(config, compoundFieldConfig);
            if (!scriptType.isEmpty()) {
                result = scriptType;
            } else {
                FieldConfig fieldConfig = configurationExplorer.getFieldConfig(objectType, config.getName());
                if (fieldConfig == null) {
                    throw new IllegalArgumentException(config.getName() + " isn't defined in type " + objectType);
                }
                SimpleSearchFieldType.Type dataType = SimpleSearchFieldType.byFieldType(fieldConfig.getFieldType());
                if (dataType != null) {
                    result = Collections.singleton(new SimpleSearchFieldType(dataType, config.getMultiValued()));
                } else {
                    result = Collections.singleton(new TextSearchFieldType(getSupportedLanguages(config),
                            config.getMultiValued(), config.getSearchBy()));
                }
            }
        }

        fieldTypeMap.put(key, result);
        return result;
    }

    private Set<SearchFieldType> getScriptType(@Nonnull IndexedFieldConfig config, CompoundFieldConfig compoundFieldConfig) {
        if (config.getScriptConfig() != null) {
            return getScriptFieldType(config.getScriptConfig().getScriptReturnType(), config.getMultiValued(), config.getSearchBy());
        } else if (compoundFieldConfig != null) {
            return getScriptFieldType(compoundFieldConfig.getScriptConfig().getScriptReturnType(), false, config.getSearchBy());
        }
        return Collections.emptySet();
    }

    private String getDoel(IndexedFieldConfig config, CompoundFieldConfig compoundFieldConfig) {
        String doel = config.getDoel();
        if (doel != null) {
            return doel;
        }
        if (compoundFieldConfig != null && compoundFieldConfig.getDoel() != null) {
            return compoundFieldConfig.getDoel();
        }
        return null;
    }

    /**
     * Возвращает набор типов, используемых при индексации поля с заданным именем в заданных областях поиска.
     * 
     * @param name имя индексируемого поля
     * @param areas набор областей поиска
     * @param srcTargetTypes набор целевых типов
     * @return набор типов индексируемых полей
     */
    public Set<SearchFieldType> getFieldTypes(String name, Collection<String> areas, Collection<String> srcTargetTypes) {
        if (name == null) {
            String msgAreas = "";
            String msgTargetTypes = "";
            if (areas != null) {
                for (String tmp : areas) {
                    msgAreas += (msgAreas.isEmpty() ? "" : ", ") + (tmp != null ? tmp : "null");
                }
            }
            if (srcTargetTypes != null) {
                for (String tmp : srcTargetTypes) {
                    msgTargetTypes += (msgTargetTypes.isEmpty() ? "" : ", ") + (tmp != null ? tmp : "null");
                }
            }
            String msg = "Search parameter/filter contains null as indexed fieldName (areas: " + msgAreas + "; targetTypes: " + msgTargetTypes + ")";
            if (logger.isErrorEnabled()) {
                logger.error(msg);
            }
            throw new FatalException(" Ошибка конфигурации поиска, обратитесь к администратору. Описание ошибки: " + msg);
            // return new HashSet<>();
        }
        Trio<String, Collection<String>, Collection<String>> key = new Trio<>(name, areas, srcTargetTypes);

        Set<SearchFieldType> result = fieldTypesMap.get(key);
        if (result != null) {
            return result;
        }

        Collection<String> targetTypes = new HashSet<>(srcTargetTypes != null ? srcTargetTypes.size() : 0);
        if (srcTargetTypes != null) {
            for (String srcTargetType : srcTargetTypes) {
                targetTypes.add(srcTargetType.toLowerCase());
            }
        }

        result = new HashSet<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            if (areas.contains(area.getName())) {
                findFieldTypes(name, area.getTargetObjects(), targetTypes, true, result);
            }
        }

        fieldTypesMap.put(key, result);
        return result;
    }

    private void findFieldTypes(String fieldName,
                                Collection<? extends IndexedDomainObjectConfig> configs,
                                Collection<String> targetTypes,
                                boolean bUseTargetTypes,
                                Set<SearchFieldType> types) {
        for (IndexedDomainObjectConfig config : configs) {
            if ((!bUseTargetTypes) || (bUseTargetTypes && targetTypes.contains(config.getType().toLowerCase()))) {
                for (IndexedFieldConfig field : config.getFields()) {
                    if (fieldName != null && fieldName.equalsIgnoreCase(field.getName())) {
                        types.addAll(getFieldTypes(field, config.getType()));
                        break;      // No more fields with this name should be in this object config
                    }
                }
                // ищем в дочерних элементах области, у которой тип совпал, без учета типа дочерних объектов
                findFieldTypes(fieldName, config.getLinkedObjects(), targetTypes, false, types);
            } else {
                // для остальных областей ищем с учетом типа дочерних объектов
                findFieldTypes(fieldName, config.getLinkedObjects(), targetTypes, true, types);
            }
        }
    }

    /**
     * Определяет, является ли доменный объект объектом вложения (содержит файл).
     * 
     * @param object доменный объект
     * @return true если переданный объект является объектом вложения
     * @throws NullPointerException если object==null
     */
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
        Pair<String, String> key = new Pair<>(attachmentType, parentType);

        String result = attachmentParentLinkNameMap.get(key);
        if (result != null) {
            return result;
        }

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
                            result = parentFieldConfig.getName();
                            attachmentParentLinkNameMap.put(key, result);
                            return result;
                        } else {
                            result = parentType;
                            attachmentParentLinkNameMap.put(key, result);
                            return result;
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
        if (supportedLanguages != null) {
            return supportedLanguages;
        }

        if (languageConfigs == null || languageConfigs.size() == 0) {
            supportedLanguages = Collections.singletonList("");
        } else {
            ArrayList<String> result = new ArrayList<>(languageConfigs.size());
            for (SearchLanguageConfig config : languageConfigs) {
                result.add(config.getLangId().trim());
            }
            supportedLanguages = result;
        }

        return supportedLanguages;
    }

    private List<String> getSupportedLanguages(IndexedFieldConfig config) {
        String lang = config.getLanguage();
        if (lang == null) {
            return getSupportedLanguages();
        }
        HashSet<String> langIds = new HashSet<>();
        String[] langs = lang.split("[\\s,:;]+");
        if (langs.length == 1 && langs[0].trim().length() == 0) {
            return Collections.singletonList("");
        }
        for (String langId : langs) {
            if (langId.trim().length() > 0) {
                langIds.add(langId.trim());
            }
        }
        return new ArrayList<>(langIds);
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
        Pair<String, String> key = new Pair<>(field, area);

        List<String> result = supportedLanguagesMap.get(key);
        if (result != null) {
            return result;
        }

        List<IndexedFieldConfig> foundFields = findIndexedFieldConfigs(field, area);
        if (foundFields.size() == 0) {
            result = Collections.emptyList();
            supportedLanguagesMap.put(key, result);
            return result;
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

        result = new ArrayList<>(langIds);
        supportedLanguagesMap.put(key, result);
        return result;
    }

    public Collection<String> findApplicableTypes(String fieldName, List<String> areaNames, List<String> targetTypes) {

        Collection<String> allTypeResult = new ArrayList<>();

        for (String targetType : targetTypes) {

            Trio<String, List<String>, String> key = new Trio<>(fieldName, areaNames, targetType);

            Collection<String> result = applicableTypesMap.get(key);
            if (result == null) {

                if (SearchFilter.EVERYWHERE.equals(fieldName)) {
                    //types = configHelper.findAllObjectTypes(areaNames, targetType);
                    result = Collections.singleton(ALL_TYPES);
                } else if (SearchFilter.CONTENT.equals(fieldName)) {
                    //types = configHelper.findObjectTypesWithContent(areaNames, targetType);
                    result = Collections.singleton(ALL_TYPES);
                } else {
                    result = findObjectTypesContainingField(fieldName, areaNames, targetType);
                }
                if (isAttachmentObjectField(fieldName)) {
                    result.addAll(findObjectTypesWithContent(areaNames, targetType));
                }

                applicableTypesMap.put(key, result);
            }
            allTypeResult.addAll(result);
        }
        return allTypeResult;
    }

    private boolean isAttachmentObjectField(String fieldName) {
        return AttachmentService.NAME.equals(fieldName)
                || AttachmentService.DESCRIPTION.equals(fieldName)
                || AttachmentService.CONTENT_LENGTH.equals(fieldName);
    }

    private Set<SearchFieldType> getScriptFieldType(IndexedFieldScriptConfig.ScriptReturnType scriptReturnType, boolean multivalued, IndexedFieldConfig.SearchBy searchBy) {
        Set<SearchFieldType> result;
        switch (scriptReturnType) {
            case BOOLEAN:
                result = Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.BOOL, multivalued));
                break;
            case DATE:
                result = Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE, multivalued));
                break;
            case LONG:
                result = Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG, multivalued));
                break;
            case DECIMAL:
                result = Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DOUBLE, multivalued));
                break;
            default:
                result = Collections.<SearchFieldType>singleton(new TextSearchFieldType(getSupportedLanguages(), multivalued, searchBy));
        }
        return result;
    }

    public void clearCache(){
        effectiveConfigsMap.clear();
        fieldTypeMap.clear();
        attachmentParentLinkNameMap.clear();
        indexedFieldConfigsMap.clear();
        supportedLanguagesMap.clear();
        fieldTypesMap.clear();
        objectTypesContainingFieldMap.clear();
        objectTypesWithContentMap.clear();
        indexedFieldConfigMap.clear();
        applicableTypesMap.clear();
    }

    public void disableIndexing(boolean disable){
        disableIndexing = disable;
    }

    public boolean isDisableIndexing(){
        return disableIndexing;
    }
}
