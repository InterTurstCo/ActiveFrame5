package ru.intertrust.cm.core.gui.impl.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Konstantin Gordeev on 14.10.2014.
 */
@Component
@Configurable
public class ContactsManager {

  private static String DO_STATUS = "status";
  private static String FIELD_NAME = "name";
  private static String QUERY_STATUS_BY_ID = "select * from status where id = {0}";

  @Autowired
  private AccessControlService accessControlService;

  @Autowired
  private DomainObjectDao domainObjectService;

  @Autowired
  private DaoUtil daoUtil;

  @Autowired
  private CollectionsService collectionsService;

  private static Logger logger = LoggerFactory.getLogger(ContactsManager.class);

  public boolean changeStatusForDo(Id doId, String status) {
    DomainObject statusDomainObject;
    statusDomainObject = getStatusByName(status);
    DomainObject savedDomainObject;
    AccessToken accessToken;
    if (accessControlService != null && domainObjectService != null) {
      if (statusDomainObject != null) {
        accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        savedDomainObject = domainObjectService.setStatus(doId, statusDomainObject.getId(), accessToken);
        domainObjectService.save(savedDomainObject, accessToken);
        return (savedDomainObject != null) ? true : false;
      } else {
        logger.error("Can`t find status domain object by name: " + status);
        return false;
      }
    } else {
      logger.error("Required services accessControlService/domainObjectService are not initialized. Can`t proceed.");
      return false;
    }
  }

  public DomainObject getStatusByName(String name) {
    DomainObject status;
    status = daoUtil.findDomainObjectByField(DO_STATUS, FIELD_NAME, name);
    return status;
  }

  public String getStatusById(Id status) {
    List<Value> params = new ArrayList<>();
    Value statusIdValue = new ReferenceValue(status);
    params.add(statusIdValue);
    if (collectionsService != null) {
      IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(QUERY_STATUS_BY_ID, params);
      if (collection != null && collection.size() > 0) {
        for (IdentifiableObject O : collection) {
          return O.getString(FIELD_NAME);
        }
      }
    }
    return null;
  }
}
