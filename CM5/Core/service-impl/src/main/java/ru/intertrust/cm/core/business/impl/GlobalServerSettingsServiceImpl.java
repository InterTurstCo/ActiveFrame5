package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.ArrayList;
import java.util.List;

public class GlobalServerSettingsServiceImpl implements GlobalServerSettingsService {


    private static final String FIELD_STRING_VALUE = "string_value";
    private static final String FIELD_LONG_VALUE = "long_value";
    private static final String FIELD_BOOLEAN_VALUE = "boolean_value";

    private static final String QUERY_STRING = "SELECT S.id,S.string_value FROM string_settings S" +
            " JOIN global_server_settings GSS ON S.id = GSS.id AND UPPER(GSS.name) = UPPER({0})";
    private static final String QUERY_LONG = "SELECT L.id,L.long_value FROM long_settings L" +
            " JOIN global_server_settings GSS ON L.id = GSS.id AND UPPER(GSS.name) = UPPER({0})";
    private static final String QUERY_BOOLEAN = "SELECT B.id,B.boolean_value FROM boolean_settings B" +
            " JOIN global_server_settings GSS ON B.id = GSS.id AND UPPER(GSS.name) = UPPER({0})";

    @Autowired
    Environment environment;

    @Autowired
    DomainObjectDao domainObjectDao;

    @Autowired
    CollectionsDao collectionsDao;

    @Autowired
    AccessControlService accessControlService;

    private String stringPparameterValue;
    private Boolean booleanPparameterValue;
    private Long longPparameterValue;

    @Override
    public String getString(String name) {

        List<Value> qParams = new ArrayList<>();
        qParams.add(new StringValue(name));

        IdentifiableObjectCollection sParams = collectionsDao.findCollectionByQuery(QUERY_STRING, qParams, 0, 0, getAccessToken());
        if (sParams.size() > 0) {
            stringPparameterValue = sParams.get(0).getString(FIELD_STRING_VALUE);
        } else {
            try {
                stringPparameterValue = environment.getRequiredProperty(name, String.class);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return stringPparameterValue;
    }

    @Override
    public String getString(String name, String defaultValue) {
        stringPparameterValue = getString(name);
        return (stringPparameterValue == null) ? defaultValue : stringPparameterValue;
    }

    @Override
    public Boolean getBoolean(String name) {
        List<Value> qParams = new ArrayList<>();
        qParams.add(new StringValue(name));
        IdentifiableObjectCollection bParams = collectionsDao.findCollectionByQuery(QUERY_BOOLEAN, qParams, 0, 0, getAccessToken());
        if (bParams.size() > 0) {
            booleanPparameterValue = bParams.get(0).getBoolean(FIELD_BOOLEAN_VALUE);
        } else {
            try {
                booleanPparameterValue = environment.getRequiredProperty(name, Boolean.class);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return booleanPparameterValue;
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        booleanPparameterValue = getBoolean(name);
        return (booleanPparameterValue == null) ? defaultValue : booleanPparameterValue;
    }

    @Override
    public Long getLong(String name) {
        List<Value> qParams = new ArrayList<>();
        qParams.add(new StringValue(name));
        IdentifiableObjectCollection lParams = collectionsDao.findCollectionByQuery(QUERY_LONG, qParams, 0, 0, getAccessToken());
        if (lParams.size() > 0) {
            longPparameterValue = lParams.get(0).getLong(FIELD_LONG_VALUE);
        } else {
            try {
                longPparameterValue = environment.getRequiredProperty(name, Long.class);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return longPparameterValue;
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        longPparameterValue = getLong(name);
        return (longPparameterValue == null) ? defaultValue : longPparameterValue;
    }

    private AccessToken getAccessToken() {
        return accessControlService.createSystemAccessToken(getClass().getName());
    }
}
