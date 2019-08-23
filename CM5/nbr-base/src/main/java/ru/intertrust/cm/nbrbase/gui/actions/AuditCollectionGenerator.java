package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;

import java.util.*;

@ServerComponent(name = "audit.collection")
public class AuditCollectionGenerator implements CollectionDataGenerator {

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired
    private CollectionsService collectionsService;

    private final Set<String> EXCLUDE_TYPES_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("attachment")));

    private final String BY_DATE_FILTER = "byDate";
    private final String BY_OPERATOR_FILTER = "byOperator";
    private final String BY_EVENT_NAME_FILTER = "byEventName";
    private final String BY_DESCRIPTION_FILTER = "byDescription";

    // Фильтр по доменным объектам, по которым будут отобраны данные аудита в коллекции, остальные будут проигнорированы (кроме логин/выход)
    private final String INCLUDE_SPECIFIC_AUDIT_DOMAIN_OBJECTS_ONLY_FILTER = "includeSpecificAuditDomainObjectsOnly";

    // набор фильтров с представления, которые может применить пользователь
    private final Set<String> COLLECTION_VIEW_FILTERS_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(BY_DATE_FILTER, BY_OPERATOR_FILTER, BY_EVENT_NAME_FILTER, BY_DESCRIPTION_FILTER)));

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        String query = generateRowQuery(filters);
        
        // Применяем фильтр
        StringBuilder whereSb = new StringBuilder(" WHERE 1=1 ");
        int filterIndex = 0;
        
        List<Value> params = new ArrayList<Value>();

        if (filters != null) {
            for (Filter filter : filters) {
                final String filterName = filter.getFilter();

                if (COLLECTION_VIEW_FILTERS_SET.contains(filterName)) {
                    whereSb.append(" AND ");

                    switch (filterName) {
                        case BY_DATE_FILTER:
                            whereSb.append(" updateddate BETWEEN {");
                            whereSb.append(filterIndex++);
                            whereSb.append("} AND {");
                            whereSb.append(filterIndex++);
                            whereSb.append("}");

                            params.add(filter.getParameterMap().get(0).get(0));
                            params.add(filter.getParameterMap().get(1).get(0));
                            break;
                        case BY_OPERATOR_FILTER:
                            whereSb.append(" LOWER(operator) LIKE LOWER({");
                            whereSb.append(filterIndex++);
                            whereSb.append("})");

                            params.add(filter.getParameterMap().get(0).get(0));
                            break;
                        case BY_EVENT_NAME_FILTER:
                            whereSb.append(" LOWER(eventname) LIKE LOWER({");
                            whereSb.append(filterIndex++);
                            whereSb.append("})");

                            params.add(filter.getParameterMap().get(0).get(0));
                            break;
                        case BY_DESCRIPTION_FILTER:
                            whereSb.append(" LOWER(description) LIKE LOWER({");
                            whereSb.append(filterIndex++);
                            whereSb.append("})");

                            params.add(filter.getParameterMap().get(0).get(0));
                            break;
                    }
                }
            }
        }
        
        // Применяем сортировку
        String sort = "";
        if (sortOrder != null) {
            for (SortCriterion sortCriterion : sortOrder) {
                if (sort.isEmpty()) {
                    sort += " order by ";
                }else {
                    sort += " , ";
                }
                sort += sortCriterion.getField() + 
                        (sortCriterion.getOrder() == null || sortCriterion.getOrder().equals(Order.DESCENDING) ? " desc " : " asc ");
            }
        }

        final String where = whereSb.toString();
        return collectionsService.findCollectionByQuery(query + where + sort, params, offset, limit);
    }

    private String generateRowQuery(List<? extends Filter> filters) {
        String result = "SELECT\r\n" + 
                "  q.\"id\" AS id,\r\n" + 
                "  q.\"eventname\" AS EventName,\r\n" + 
                "  q.\"description\" as Description,\r\n" + 
                "  q.\"updateddate\" as UpdatedDate,\r\n" + 
                "  q.\"operator\" as Operator\r\n" + 
                "FROM(\r\n" + 
                "SELECT\r\n" + 
                "  log1.\"id\"                                                                                                AS id,\r\n" + 
                "  log1.\"updated_date\"                                                                                      AS UpdatedDate,\r\n" + 
                "  (CASE WHEN log1.\"person\" IS NOT NULL THEN\r\n" + 
                "    pr2.\"login\" || ' (' || trim(COALESCE(pr2.\"lastname\", '') || ' ' || COALESCE(pr2.\"firstname\", '')) ||')'\r\n" + 
                "   WHEN pr1.\"login\" is NOT NULL THEN pr1.\"login\" || ' (' || trim(COALESCE(pr1.\"lastname\", '') || ' ' || COALESCE(pr1.\"firstname\", '')) ||')'\r\n" + 
                "   else log1.\"user_id\" END)                                                                                              AS Operator,\r\n" + 
                "  log1.\"event_type\"                                                                                        AS EventName,\r\n" + 
                "  (CASE WHEN log1.\"success\" = 1 and (log1.\"person\" IS NOT NULL OR log1.\"event_type\" = 'LOGOUT')\r\n" + 
                "    THEN 'Успешно'\r\n" + 
                "   ELSE 'Не успешно' END) || COALESCE(' (ip: ' || log1.\"client_ip_address\" || ')','')                      AS Description\r\n" + 
                "FROM\r\n" + 
                "  user_event_log log1\r\n" + 
                "  LEFT JOIN person pr2 ON pr2.\"id\" = log1.\"person\"\r\n" + 
                "  LEFT JOIN person pr1 on pr1.\"login\" = log1.\"user_id\"\r\n";
        
        // Получаем все типы, у которых включен аудит
        Set<String> typeWithAudit = getAllTypesWithAudit(filters);
        
        // Генерим UNION
        for (String typeName : typeWithAudit) {
            result += "union\r\n";
            result += "  SELECT\r\n" + 
                    "    tCred.\"id\"                                                                                                 AS id,\r\n" + 
                    "    tCred.\"updated_date\"                                                                                       AS UpdatedDate,\r\n" + 
                    "    pr2.\"login\" || ' (' || trim(COALESCE(pr2.\"lastname\", '') || ' ' || COALESCE(pr2.\"firstname\", '')) ||\r\n" + 
                    "    ')'                                                                                                      AS Operator,\r\n" + 
                    "    (CASE WHEN tCred.\"operation\" = 1\r\n" + 
                    "      THEN 'Создание'\r\n" + 
                    "     WHEN tCred.\"operation\" = 2\r\n" + 
                    "       THEN 'Изменение'\r\n" + 
                    "     WHEN tCred.\"operation\" = 3\r\n" + 
                    "       THEN 'Удаление'\r\n" + 
                    "     END)                                                                                                    AS EventName,\r\n" + 
                    "     dt.name                                                      AS Description\r\n" + 
                    "  FROM\r\n" + 
                    "    " + typeName + "_al tCred\r\n" + 
                    "    LEFT JOIN person pr2 ON pr2.\"id\" = tCred.\"updated_by\"\r\n "
                    + "join domain_object_type_id dt on dt.id = tCred.domain_object_id_type\r\n";
        }
        
        result +=") q";
        
        return result;
    }

    /**
     * Метод возвращает все рутовые типы у которых включен аудит, кроме набора исключений.
     * Аудит включен или непосредственно у рута или у наследника
     * @return
     */
    private Set<String> getAllTypesWithAudit(List<? extends Filter> filters) {
        final Set<String> includeSpecificAuditDOsSet = getIncludeSpecificDosSet(filters);
        Set<String> result = new HashSet<String>();
        
        Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {

            // Проверяем включена ли глобальная настройка аудита
            final String domainObjectType = domainObjectTypeConfig.getName();

            final boolean isAuditLogEnable = isAuditLogEnable(domainObjectTypeConfig);
            final boolean isAuditLogType = configurationExplorer.isAuditLogType(domainObjectType);

            if (isAuditLogEnable && !isAuditLogType) {
                final String rootDomainObjectType = configurationExplorer.getDomainObjectRootType(domainObjectType).toLowerCase();

                // исключаем некоторые типы без аудита
                if (!EXCLUDE_TYPES_SET.contains(rootDomainObjectType)) {

                    // включаем для аудита только типы, указанные в фильтре, если он был задействован
                    if (!CollectionUtils.isEmpty(includeSpecificAuditDOsSet)) {
                        if (includeSpecificAuditDOsSet.contains(rootDomainObjectType)) {
                            result.add(rootDomainObjectType);
                        }
                        // иначе добавляем сразу
                    } else {
                        result.add(rootDomainObjectType);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Возвращает список специфичных типов, по которым нужно показать данные аудита в коллекции.<br>
     * Список берется из фильтра 'includeSpecificAuditDomainObjectsOnly', если он был задействован.
     *
     * @param filters список всех фильтров
     * @return регистронезависимый набор имен доменных объектов для аудита из фильтра,<br>
     * либо null, если фильтр не был применен или список параметров в нем пуст.
     */
    private Set<String> getIncludeSpecificDosSet(List<? extends Filter> filters) {
        final Filter includeSpecificAuditDomainObjectsOnlyFilter = FilterBuilderUtil.getFilterByName(INCLUDE_SPECIFIC_AUDIT_DOMAIN_OBJECTS_ONLY_FILTER, filters);
        if (includeSpecificAuditDomainObjectsOnlyFilter != null) {

            final HashMap<Integer, List<Value>> parameterMap = includeSpecificAuditDomainObjectsOnlyFilter.getParameterMap();
            final Collection<List<Value>> valuesListsCollection = parameterMap.values();

            final int parametersCount = valuesListsCollection.size();
            if (parametersCount > 0) {

                // делаем набор регистронезависимым
                Set<String> parametersSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

                valuesListsCollection.forEach(valuesList -> {
                    final StringValue stringValue = (StringValue) valuesList.get(0);
                    final String includeDoNameFilterValue = stringValue.get();

                    // обрезаем символы '%' в параметрах, добавляемые автоматически в строковые значения фильтра
                    final String includeDoName = FilterBuilderUtil.cutPercentsCharacters(includeDoNameFilterValue);
                    parametersSet.add(includeDoName);
                });
                return parametersSet;
            }
        }
        return null;
    }

    /**
     * Проверка включен ли аудит для типа
     * @param domainObjectTypeConfig
     * @return
     */
    private boolean isAuditLogEnable(DomainObjectTypeConfig domainObjectTypeConfig) {
        boolean result = false;

        // Если в конфигурации доменного объекта указан флаг включения аудит
        // лога то принимаем его
        if (domainObjectTypeConfig.isAuditLog() != null) {
            result = domainObjectTypeConfig.isAuditLog();
        } else {
            // Если в конфигурации доменного объекта НЕ указан флаг включения
            // аудит лога то принимаем конфигурацию из блока глобальной
            // конфигурации
            GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();

            if (globalSettings != null && globalSettings.getAuditLog() != null) {
                result = globalSettings.getAuditLog().isEnable();
            }
        }
        return result;
    }    
    
    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        // TODO Оптимизировать запрос
        return findCollection(filterValues, null, 0, 0).size();
    }

    private String generateCountQuery() {
        // TODO Auto-generated method stub
        return "";
    }

}
