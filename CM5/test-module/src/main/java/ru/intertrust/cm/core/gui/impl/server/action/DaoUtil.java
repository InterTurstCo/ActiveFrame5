package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 22.05.14
 *         Time: 11:32
 */
@Component
public class DaoUtil {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessService;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private ConfigurationService configurationService;

    private static final String ELLIPSES = "...";

    public DomainObject findDomainObjectByField(String typeName, String fieldName, String fieldValue) {
        if (typeName == null || fieldName == null || fieldValue == null) {
            return null;
        }
        String query = String.format("select * from %s where %s = {0}", typeName, fieldName);
        List<DomainObject> collection =  findCollectionByQuery(query, fieldValue);

        if (collection != null && collection.size() > 0) {
            return collection.get(0);
        }
        return null;
    }

    public DomainObject createDomainObject(String typeName) {
        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setTypeName(typeName);
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);
        return domainObject;
    }

    public DomainObject save(DomainObject domainObject) {
        return save(domainObject, false);
    }

    public DomainObject save(DomainObject domainObject, boolean truncate) {
        ensureFieldsLength(domainObject, truncate);
        return domainObjectDao.save(domainObject, createAccessToken());
    }

    public List<DomainObject> save(List<DomainObject> domainObjects) {
        return domainObjectDao.save(domainObjects, createAccessToken());
    }

    public DomainObject find(Id id) {
       return domainObjectDao.find(id, createAccessToken());
    }

    public List<DomainObject> findAll(String typeName) {
        return domainObjectDao.findAll(typeName, createAccessToken());
    }

    public List<DomainObject> findLinkedDomainObjects(DomainObject parent, String childDO, String fieldName) {
        return domainObjectDao.findLinkedDomainObjects(parent.getId(), childDO, fieldName, createAccessToken());
    }

    public List<DomainObject> findCollectionByQuery(String query, String... args) {
        List<Value> params = new ArrayList<>();
        for (String arg : args) {
            Value value = new StringValue(arg);
            params.add(value);
        }
        IdentifiableObjectCollection collection = collectionsDao.findCollectionByQuery(query, params, 0, 0, createAccessToken());
        if (collection == null) {
            return null;
        }
        List<DomainObject> list = new ArrayList<>();
        for (int i = 0; i < collection.size(); i++ ) {
            list.add(find(collection.getId(i)));
        }
        return list;
    }

    private AccessToken createAccessToken() {
        return accessService.createSystemAccessToken(this.getClass().getName());
    }

    private void ensureFieldsLength(DomainObject domainObject, boolean truncate) {
        DomainObjectTypeConfig doConfig = configurationService.getConfig(DomainObjectTypeConfig.class, domainObject.getTypeName());
        for (FieldConfig fieldConfig : doConfig.getFieldConfigs()) {
            if (fieldConfig.getFieldType() == FieldType.STRING) {
                String fieldName = fieldConfig.getName();
                String value = domainObject.getString(fieldName);
                if (value != null) {
                    StringFieldConfig stringFieldConfig = (StringFieldConfig)fieldConfig;
                    int declaredLength = stringFieldConfig.getLength();
                    int actualLength = value.length();
                    if (actualLength > declaredLength ) {
                        if (truncate) {
                            domainObject.setString(fieldConfig.getName(), value.substring(0,
                                    declaredLength - ELLIPSES.length()) + ELLIPSES);
                        } else {
                            throw new RuntimeException(String.format("Длина поля %s превышает допустимую: " +
                                    "должно быть: %d, обнаружено: %d", fieldName, declaredLength, actualLength));
                        }
                    }
                }
            }
        }
    }
}
