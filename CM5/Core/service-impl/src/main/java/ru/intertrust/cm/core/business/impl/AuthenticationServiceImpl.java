package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.dao.api.AuthenticationDAO;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;

import java.util.Date;

/**
 * Реализация сервиса для работы с доменным объектом объектом Authentication Info
 * @author atsvetkov
 *
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private MD5Service md5Service;

    private AuthenticationDAO authenticationDAO;

    private CrudServiceDAO crudServiceDAO;

    @Autowired
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
        String passwordHash = md5Service.getMD5(authenticationInfo.getPassword());


        DomainObject authInfo = new GenericDomainObject();
        authInfo.setTypeName("Authentication Info");
        Date currentDate = new Date();
        authInfo.setCreatedDate(currentDate);
        authInfo.setModifiedDate(currentDate);
        StringValue password = new StringValue(passwordHash);
        authInfo.setValue("Password", password);
        authInfo.setValue("User Uid", new StringValue(authenticationInfo.getUserUid()));
        DomainObjectConfig domainObjectConfig = configurationExplorer.getDomainObjectConfig(authInfo
                .getTypeName());
        DomainObject createdAuthInfo = crudServiceDAO.create(authInfo, domainObjectConfig);

        RdbmsId id  = (RdbmsId)createdAuthInfo.getId();
        DomainObject role = new GenericDomainObject();
        role.setTypeName("Employee Role");
        role.setCreatedDate(currentDate);
        role.setModifiedDate(currentDate);
        StringValue roleName = new StringValue(authenticationInfo.getRole());
        role.setValue("Role", roleName);
        role.setValue("Authentication Info", new IntegerValue(id.getId()));
        domainObjectConfig = configurationExplorer.getDomainObjectConfig(role.getTypeName());
        crudServiceDAO.create(role, domainObjectConfig);
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
     * @param authenticationDAO
     */
    public void setAuthenticationDAO(AuthenticationDAO authenticationDAO) {
        this.authenticationDAO = authenticationDAO;
    }

}
