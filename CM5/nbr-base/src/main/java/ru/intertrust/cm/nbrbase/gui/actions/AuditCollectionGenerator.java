package ru.intertrust.cm.nbrbase.gui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;


import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

@ServerComponent(name = "audit.collection")
public class AuditCollectionGenerator implements CollectionDataGenerator{

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired
    private CollectionsService collectionsService;
    
    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        String query = generateRowQuery();
        
        // Применяем фильтр
        String where = "";
        int filterIndex = 0;
        
        List<Value> params = new ArrayList<Value>();
        
        if (filters != null) {
            for (Filter filter : filters) {
                if (where.isEmpty()) {
                    where += " where ";
                }else {
                    where += " and ";
                }
                
                if (filter.getFilter().equals("byDate")) {
                    where += " updateddate between {" + filterIndex++ + "} and {" + filterIndex++ + "}";
                    params.add(filter.getParameterMap().get(0).get(0));
                    params.add(filter.getParameterMap().get(1).get(0));
                }else if(filter.getFilter().equals("byOperator")) {
                    where += " operator like {" + filterIndex++ + "}";
                    params.add(filter.getParameterMap().get(0).get(0));
                }else if(filter.getFilter().equals("byEventName")) {
                    where += " eventname like {" + filterIndex++ + "}";
                    params.add(filter.getParameterMap().get(0).get(0));
                }else if(filter.getFilter().equals("byDescription")) {
                    where += " description like {" + filterIndex++ + "}";
                    params.add(filter.getParameterMap().get(0).get(0));
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
        
        return collectionsService.findCollectionByQuery(query + where + sort, params, offset, limit);
    }

    private String generateRowQuery() {
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
        Set<String> typeWithAudit = getAllTypesWithAudit(); 
        
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
     * Метод возвращает все рутовые типы у которых включен аудит.
     * Аудит включен или непосредственно у рута или у наследника
     * @return
     */
    private Set<String> getAllTypesWithAudit() {
        Set<String> result = new HashSet<String>();
        
        Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {
            // Проверяем включена ли глобальная настройка аудита
            if (isAuditLogEnable(domainObjectTypeConfig) && !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                result.add(configurationExplorer.getDomainObjectRootType(domainObjectTypeConfig.getName()).toLowerCase());
            }
        }
        return result;
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
