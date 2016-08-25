package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.StatusDao;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_DO;

/**
 * Реализация сервиса работы со статусами.
 * @author atsvetkov
 */
public class StatusDaoImpl implements StatusDao {

    private Map<Id, String> idToStatusName = new ConcurrentHashMap<>();
    private Map<String, Id> statusNameToId = new ConcurrentHashMap<>();

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

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
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Map<String, Value> uniqueKeyValuesByName = new HashMap<>();
        uniqueKeyValuesByName.put("name", new StringValue(statusName));

        DomainObject statusDO = domainObjectDao.findByUniqueKey(STATUS_DO, uniqueKeyValuesByName, accessToken);

        if (statusDO == null) {
            throw new IllegalArgumentException("Status not found: " + statusName);
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
        if (statusId == null) {
            throw new IllegalArgumentException("StatusId is null");
        }

        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        DomainObject statusDO = domainObjectDao.find(statusId, accessToken);

        if (statusDO == null) {
            throw new IllegalArgumentException("Status not found: " + statusId);
        }

        return statusDO.getString("Name");
    }

    @Override
    public void resetCache() {
        idToStatusName.clear();
        statusNameToId.clear();
    }
}
