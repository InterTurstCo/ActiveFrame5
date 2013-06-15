package ru.intertrust.cm.core.business.impl;

import java.util.Date;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.GenericBusinessObject;
import ru.intertrust.cm.core.business.api.dto.IntegerValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;

/**
 * Реализация сервиса для работы с бизнес-объектом Person
 * @author atsvetkov
 *
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private MD5Service md5Service;

    private AuthenticationDAO authenticationDAO;

    private CrudServiceDAO crudServiceDAO;

    private ConfigurationExplorer configurationExplorer;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setCrudServiceDAO(CrudServiceDAO crudServiceDAO) {
        this.crudServiceDAO = crudServiceDAO;
    }

    /**
     * Добавляет пользователя в базу. Кодирует пароль, использую MD5 алгоритм. В
     * базу сохраняется MD5 хеш значение пароля.
     * @param authenticationInfo
     *            {@link AuthenticationInfoAndRole}
     */
    @Override
    public void insertAuthenticationInfoAndRole(AuthenticationInfoAndRole authenticationInfo) {
        String enteredPassword = authenticationInfo.getPassword();
        String passwordHash = md5Service.getMD5(enteredPassword);
        authenticationInfo.setPassword(passwordHash);

        authenticationDAO.insertAuthenticationInfo(authenticationInfo);

        BusinessObject authInfo = new GenericBusinessObject();
        authInfo.setTypeName("Authentication Info");
        Date currentDate = new Date();
        authInfo.setCreatedDate(currentDate);
        authInfo.setCreatedDate(currentDate);
        StringValue password = new StringValue(authenticationInfo.getPassword());
        authInfo.setValue("Password", password);
        authInfo.setValue("User Uid", new StringValue(authenticationInfo.getUserUid()));

        BusinessObjectConfig businessObjectConfig = configurationExplorer.getBusinessObjectConfig(authInfo
                .getTypeName());
        BusinessObject createdAuthInfo = crudServiceDAO.create(authInfo, businessObjectConfig );

        RdbmsId id  = (RdbmsId)createdAuthInfo.getId();

        BusinessObject role = new GenericBusinessObject();
        role.setTypeName("Employee Role");
        role.setCreatedDate(currentDate);
        role.setCreatedDate(currentDate);
        StringValue roleName = new StringValue(authenticationInfo.getRole());
        role.setValue("Role", roleName);
        role.setValue("Authentication Info", new IntegerValue(id.getId()));

        businessObjectConfig = configurationExplorer.getBusinessObjectConfig(role
                .getTypeName());

        crudServiceDAO.create(role, businessObjectConfig);
    }

    /**
     * Проверяет сужествует ли пользователь с указанным логином.
     * @param login
     *            логин пользователя
     * @return true, если существует пользователь, false иначе
     */
    @Override
    public boolean existsAuthenticationInfo(String login) {
        return authenticationDAO.existsAuthenticationInfo(login);
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
     * Устанавливает {@see #authenticationDAO}.
     * @param personDAO
     */
    public void setAuthenticationDAO(AuthenticationDAO authenticationDAO) {
        this.authenticationDAO = authenticationDAO;
    }

}
