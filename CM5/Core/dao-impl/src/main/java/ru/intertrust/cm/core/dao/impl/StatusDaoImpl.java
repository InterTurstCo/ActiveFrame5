package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatusDao;
import ru.intertrust.cm.core.dao.impl.utils.SingleObjectRowMapper;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация сервиса работы со статусами.
 * @author atsvetkov
 */
public class StatusDaoImpl implements StatusDao {

    private Map<Id, String> idToStatusName = new HashMap<Id, String>();
    private Map<String, Id> statusNameToId = new HashMap<String, Id>();

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private DomainObjectCacheService domainObjectCacheService;

    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }


    @Override
    public Id getStatusIdByName(String statusName) {
        Id statusId = null;
        if (statusNameToId.get(statusName) == null) {
            statusId = readStatusIdByName(statusName);
            statusNameToId.put(statusName, statusId);
        } else {
            statusId = statusNameToId.get(statusName);

        }
        return statusId;
    }

    private Id readStatusIdByName(String statusName) {
        String query = "select s.* from " + wrap(GenericDomainObject.STATUS_DO) + " s where s." + wrap("name") + "=:name";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("name", statusName);
        DomainObject statusDO = jdbcTemplate.query(query, paramMap,
                new SingleObjectRowMapper(GenericDomainObject.STATUS_DO, configurationExplorer,
                        domainObjectTypeIdCache));
        if (statusDO == null) {
            throw new IllegalArgumentException("Status not found: " + statusName);
        } else {
            domainObjectCacheService.putOnRead(statusDO);
        }
        return statusDO.getId();
    }

    @Override
    public String getStatusNameById(Id statusId) {
        String statusName = null;
        if (idToStatusName.get(statusId) == null) {
            statusName = readStatusNameById(statusId);
            idToStatusName.put(statusId, statusName);
        } else {
            statusName = idToStatusName.get(statusId);
        }
        return statusName;
    }

    private String readStatusNameById(Id statusId) {
        String query = "select s.* from " + wrap(GenericDomainObject.STATUS_DO) + " s where s." + wrap("id") + "=:id";
        Map<String, Object> paramMap = new HashMap<String, Object>();

        if (statusId == null) {
            throw new IllegalArgumentException("StatusId is null");
        }

        paramMap.put("id", ((RdbmsId) statusId).getId());
        DomainObject statusDO = jdbcTemplate.query(query, paramMap,
                new SingleObjectRowMapper(GenericDomainObject.STATUS_DO, configurationExplorer,
                        domainObjectTypeIdCache));

        if (statusDO == null) {
            throw new IllegalArgumentException("Status not found: "
                    + statusId);
        } else {
            domainObjectCacheService.putOnRead(statusDO);
        }
        return statusDO.getString("Name");
    }

    @Override
    public void resetCache() {
        idToStatusName.clear();
        statusNameToId.clear();
    }
}
