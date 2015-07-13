package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AuthenticationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация DAO для работы с системным объектом AuthenticationInfo.
 * @author atsvetkov
 *
 */
public class AuthenticationDaoImpl implements AuthenticationDao {

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.AuthenticationDao#existsAuthenticationInfo(java.lang.String)
     */
    @Override
    public boolean existsAuthenticationInfo(String userUid) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

        Map<String, Value> uniqueKeyValuesByName = new HashMap<>();
        uniqueKeyValuesByName.put("user_uid", new StringValue(userUid));

        DomainObject authenticationInfo =
                domainObjectDao.findByUniqueKey("authentication_info", uniqueKeyValuesByName, accessToken);

        return authenticationInfo != null;
    }

}
