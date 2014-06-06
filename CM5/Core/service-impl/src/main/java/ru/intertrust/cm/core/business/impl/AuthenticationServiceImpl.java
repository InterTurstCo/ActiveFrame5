package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AuthenticationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Реализация сервиса для работы с доменным объектом объектом Authentication Info
 * @author atsvetkov
 *
 */
public class AuthenticationServiceImpl implements AuthenticationService {
   
    
    private MD5Service md5Service;

    private AuthenticationDao authenticationDao;

    private DomainObjectDao domainObjectDao;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private PersonManagementServiceDao personManagementServiceDao;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }
    
    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Добавляет пользователя в базу. 
     * @param authenticationInfo
     *            {@link AuthenticationInfoAndRole}
     */
    @Override
    public void insertAuthenticationInfoAndRole(AuthenticationInfoAndRole authenticationInfo) {

        GenericDomainObject authInfo = new GenericDomainObject();
        authInfo.setTypeName("Authentication_Info");
        Date currentDate = new Date();
        authInfo.setCreatedDate(currentDate);
        authInfo.setModifiedDate(currentDate);
        StringValue password = new StringValue(authenticationInfo.getPassword());
        authInfo.setValue("Password", password);
        authInfo.setValue("User_Uid", new StringValue(authenticationInfo.getUserUid()));

        AccessToken accessToken = accessControlService.createSystemAccessToken("AuthenticationService");
        DomainObject createdAuthInfo = domainObjectDao.create(authInfo, accessToken);

        RdbmsId id  = (RdbmsId)createdAuthInfo.getId();
        GenericDomainObject role = new GenericDomainObject();
        role.setTypeName("Employee_Role");
        role.setCreatedDate(currentDate);
        role.setModifiedDate(currentDate);
        StringValue roleName = new StringValue(authenticationInfo.getRole());
        role.setValue("Role", roleName);
        role.setValue("Authentication_Info", new ReferenceValue(id));
        domainObjectDao.create(role, accessToken);

        Id adminGroupId = personManagementServiceDao.getGroupId(GenericDomainObject.ADMINISTRATORS_STATIC_GROUP);

        if (adminGroupId == null) {
            throw new FatalException("Administrators group not found.");
        }
        List<DomainObject> personsInAdministrators = personManagementServiceDao.getAllPersonsInGroup(adminGroupId);
        if (personsInAdministrators == null || personsInAdministrators.size() == 0) {
            DomainObject adminPerson = createAdminPerson(authenticationInfo, currentDate, accessToken);
            personManagementServiceDao.addPersonToGroup(adminGroupId, adminPerson.getId());
        }

    }

    private DomainObject createAdminPerson(AuthenticationInfoAndRole authenticationInfo, Date currentDate,
            AccessToken accessToken) {
        //Создание персоны администратора
        GenericDomainObject person = new GenericDomainObject();
        person.setTypeName("Person");
        person.setCreatedDate(currentDate);
        person.setModifiedDate(currentDate);
        person.setString("Login", authenticationInfo.getUserUid());
        person.setString("FirstName", authenticationInfo.getUserUid());
        person.setString("Login", authenticationInfo.getUserUid());
        person.setString("EMail", authenticationInfo.getUserUid() + "@localhost.com");        
        DomainObject adminPerson = domainObjectDao.create(person, accessToken);

        if (adminPerson == null || adminPerson.getId() == null) {
            throw new FatalException("Admin peson was not created.");
        }
        return adminPerson;
    }

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login
     *            логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    @Override
    public boolean existsAuthenticationInfo(String login) {
        return authenticationDao.existsAuthenticationInfo(login);
    }

    /**
     * Устанавливает {@see #md5Service}. Используется для кодирования паролей
     * пользователей.
     *
     * @param md5Service
     */
    public void setMd5Service(MD5Service md5Service) {
        this.md5Service = md5Service;
    }

    /**
     * Устанавливает {@see #authenticationDao}.
     * @param authenticationDao
     */
    public void setAuthenticationDao(AuthenticationDao authenticationDao) {
        this.authenticationDao = authenticationDao;
    }

}
