package ru.intertrust.cm.core.business.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class InitialDataLoader {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectDao domainObjectDao;
    
    @Autowired
    private AccessControlService accessControlService;
    
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    public InitialDataLoader() {
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Устанавливает {@link #authenticationService}
     * @param authenticationService сервис для работы с конфигурацией доменных объектов
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Устанавливает {@link #domainObjectDao}
     * @param domainObjectDao сервис для базовых операций с доменными объектами
     */
    public void setDomainObjectDao(DomainObjectDao domainObjectDao) {
        this.domainObjectDao = domainObjectDao;
    }
        
    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        // статусы сохраняются до сохранения остальных доменных объектов, т.к. ДО могут использовать статусы при
        // сохранении
        saveInitialStatuses();
        if (!authenticationService.existsAuthenticationInfo(ADMIN_LOGIN)) {
            insertAdminAuthenticationInfo();
        }
    }

    private void saveInitialStatuses() {
        Set<String> initialStatuses = new HashSet<String>();
        Collection<DomainObjectTypeConfig> domainObjectTypes =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);

        for (DomainObjectTypeConfig domainObjectType : domainObjectTypes) {
            String initialStatus = domainObjectType.getInitialStatus();
            if (initialStatus != null) {
                initialStatuses.add(initialStatus);
            }
        }

        for (String initialStatus : initialStatuses) {
            if (!existsStatus(initialStatus)) {
                saveStatus(initialStatus);
            }

        }
    }

    private boolean existsStatus(String statusName) {
        String query = "select count(*) from Status s where s.name=:name";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("name", statusName);
        @SuppressWarnings("deprecation")
        int total = jdbcTemplate.queryForInt(query, paramMap);
        return total > 0;
    }

    private void saveStatus(String statusName) {
        GenericDomainObject statusDO = new GenericDomainObject();
        statusDO.setTypeName(DomainObjectDao.STATUS_DO);
        Date currentDate = new Date();
        statusDO.setCreatedDate(currentDate);
        statusDO.setModifiedDate(currentDate);
        statusDO.setString("Name", statusName);
        AccessToken accessToken = accessControlService.createSystemAccessToken("InitialDataLoader");
        domainObjectDao.save(statusDO, accessToken);
    }

    private void insertAdminAuthenticationInfo() {
        AuthenticationInfoAndRole admin = new AuthenticationInfoAndRole();
        admin.setUserUid(ADMIN_LOGIN);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setRole("admin");
        authenticationService.insertAuthenticationInfoAndRole(admin);
    }

}
