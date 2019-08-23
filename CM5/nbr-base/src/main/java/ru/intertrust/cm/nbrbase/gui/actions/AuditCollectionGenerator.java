package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

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
        StringBuilder sortSb = new StringBuilder();
        if (sortOrder != null) {
            for (SortCriterion sortCriterion : sortOrder) {
                if (sortSb.length() == 0) {
                    sortSb.append(" ORDER BY ");
                } else {
                    sortSb.append(" , ");
                }
                sortSb.append(sortCriterion.getField()).append(sortCriterion.getOrder() == null || sortCriterion.getOrder().equals(Order.DESCENDING) ? " DESC " : " ASC ");
            }
        }

        final String where = whereSb.toString();
        final String sort = sortSb.toString();
        return collectionsService.findCollectionByQuery(query + where + sort, params, offset, limit);
    }

    private String generateRowQuery(List<? extends Filter> filters) {
        StringBuilder resultSb = new StringBuilder();
        resultSb.append("SELECT\r\n");
        resultSb.append("  q.\"id\" AS id,\r\n");
        resultSb.append("  q.\"eventname\" AS EventName,\r\n");
        resultSb.append("  q.\"description\" AS Description,\r\n");
        resultSb.append("  q.\"updateddate\" AS UpdatedDate,\r\n");
        resultSb.append("  q.\"operator\" AS Operator\r\n");
        resultSb.append("FROM(\r\n");
        resultSb.append("SELECT\r\n");
        resultSb.append("  log1.\"id\"                                                                                                AS id,\r\n");
        resultSb.append("  log1.\"updated_date\"                                                                                      AS UpdatedDate,\r\n");
        resultSb.append("  (CASE WHEN log1.\"person\" IS NOT NULL THEN\r\n");
        resultSb.append("    pr2.\"login\" || ' (' || trim(COALESCE(pr2.\"lastname\", '') || ' ' || COALESCE(pr2.\"firstname\", '')) ||')'\r\n");
        resultSb.append("   WHEN pr1.\"login\" is NOT NULL THEN pr1.\"login\" || ' (' || trim(COALESCE(pr1.\"lastname\", '') || ' ' || COALESCE(pr1.\"firstname\", '')) ||')'\r\n");
        resultSb.append("   else log1.\"user_id\" END)                                                                                              AS Operator,\r\n");
        resultSb.append("  log1.\"event_type\"                                                                                        AS EventName,\r\n");
        resultSb.append("  (CASE WHEN log1.\"success\" = 1 AND (log1.\"person\" IS NOT NULL OR log1.\"event_type\" = 'LOGOUT')\r\n");
        resultSb.append("    THEN 'Успешно'\r\n");
        resultSb.append("   ELSE 'Не успешно' END) || COALESCE(' (ip: ' || log1.\"client_ip_address\" || ')','')                      AS Description\r\n");
        resultSb.append("FROM\r\n");
        resultSb.append("  user_event_log log1\r\n");
        resultSb.append("  LEFT JOIN person pr2 ON pr2.\"id\" = log1.\"person\"\r\n");
        resultSb.append("  LEFT JOIN person pr1 ON pr1.\"login\" = log1.\"user_id\"\r\n");

        // Получаем все типы, у которых включен аудит
        Set<String> typeWithAudit = getAllTypesWithAudit(filters);

        // Генерим UNION
        for (String typeName : typeWithAudit) {
            resultSb.append("UNION\r\n");
            resultSb.append("  SELECT\r\n");
            resultSb.append("    tCred.\"id\"                                                                                                 AS id,\r\n");
            resultSb.append("    tCred.\"updated_date\"                                                                                       AS UpdatedDate,\r\n");
            resultSb.append("    pr2.\"login\" || ' (' || trim(COALESCE(pr2.\"lastname\", '') || ' ' || COALESCE(pr2.\"firstname\", '')) ||\r\n");
            resultSb.append("    ')'                                                                                                          AS Operator,\r\n");
            resultSb.append("    (CASE WHEN tCred.\"operation\" = 1\r\n");
            resultSb.append("      THEN 'Создание'\r\n");
            resultSb.append("     WHEN tCred.\"operation\" = 2\r\n");
            resultSb.append("       THEN 'Изменение'\r\n");
            resultSb.append("     WHEN tCred.\"operation\" = 3\r\n");
            resultSb.append("       THEN 'Удаление'\r\n");
            resultSb.append("     END)                                                                                                         AS EventName,\r\n");
            resultSb.append(getDescriptionQueryPart(typeName));
            resultSb.append("  FROM\r\n");
            resultSb.append("    ");
            resultSb.append(typeName);
            resultSb.append("_al tCred\r\n");
            resultSb.append("    LEFT JOIN person pr2 ON pr2.\"id\" = tCred.\"updated_by\"\r\n ");
            resultSb.append("    INNER JOIN domain_object_type_id dt ON dt.id = tCred.domain_object_id_type\r\n");
            resultSb.append(getJoinQueryPart(typeName));
        }
        resultSb.append(") q");

        final String result = resultSb.toString();
        return result;
    }

    protected String getDescriptionQueryPart(String typeName) {
        return " dt.name AS Description\r\n";
    }

    protected String getJoinQueryPart(String typeName) {
        return "";
    }

    /**
     * Метод возвращает все рутовые типы у которых включен аудит, кроме набора исключений.
     * Аудит включен или непосредственно у рута или у наследника
     *
     * @return
     */
    protected Set<String> getAllTypesWithAudit(List<? extends Filter> filters) {
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
                    result.add(rootDomainObjectType);
                }
            }
        }
        return result;
    }

    /**
     * Проверка включен ли аудит для типа
     *
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
