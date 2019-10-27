package ru.intertrust.cm.core.business.impl;

import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_DO;
import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.USER_GROUP_DOMAIN_OBJECT;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.StaticGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupService;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
@Stateless
@Local(InitialDataLoader.class)
@Remote(InitialDataLoader.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
public class InitialDataLoaderImpl implements InitialDataLoader {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PSSWRD = "admin";

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;
    
    @Autowired
    private PersonManagementServiceDao personManagementServiceDao;

    @Autowired
    private PersonServiceDao personServiceDao;

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Resource
    private EJBContext ejbContext;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public InitialDataLoaderImpl() {
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
    
    protected Id createUserGroup(String dynamicGroupName) {
        Id userGroupId;
        GenericDomainObject userGroupDO = new GenericDomainObject();
        userGroupDO.setTypeName(USER_GROUP_DOMAIN_OBJECT);
        userGroupDO.setString("group_name", dynamicGroupName);

        AccessToken accessToken = accessControlService.createSystemAccessToken("InitialDataLoader");
        DomainObject updatedObject = domainObjectDao.save(userGroupDO, accessToken);
        userGroupId = updatedObject.getId();
        return userGroupId;
    }


    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        // Workaround for JBoss7 bug in @RunAs
        ejbContext.getContextData().put(CurrentUserAccessor.INITIAL_DATA_LOADING, true);

        // статусы сохраняются до сохранения остальных доменных объектов, т.к. ДО могут использовать статусы при
        // сохранении
        saveInitialStatuses();

        saveStaticGroups();

        if (!authenticationService.existsAuthenticationInfo(ADMIN_LOGIN)) {
            insertSuperUserAuthenticationInfo();
        }
        if (!authenticationService.existsAuthenticationInfo(GenericDomainObject.ADMINISTRATOR_LOGIN)) {
            insertAdministratorAuthenticationInfo();
        }
    }

    /**
     * Создает все статические группы кроме {@link InitialDataLoaderImpl#SUPER_USERS_STATIC_GROUP}.
     */
    private void saveStaticGroups() {
        Set<String> staticGroups = new HashSet<String>();

        Collection<StaticGroupConfig> staticGroupConfigs =
                configurationExplorer.getConfigs(StaticGroupConfig.class);

        for (StaticGroupConfig staticGroup : staticGroupConfigs) {
            String groupName = staticGroup.getName();
            if (groupName != null) {
                staticGroups.add(groupName);
            }
        }

        for (String staticGroupName : staticGroups) {
            if (!existsStaticGroup(staticGroupName)) {
                createUserGroup(staticGroupName);
            }
        }
    }
    
    // TODO Delete
    private void addAdminUserToSuperUsers() {
        Id adminId = personServiceDao.findPersonByLogin(ADMIN_LOGIN).getId();
        Id superUsersGroupId = dynamicGroupService.getUserGroupByGroupName(GenericDomainObject.SUPER_USERS_STATIC_GROUP);
        if (adminId == null || superUsersGroupId == null) {
            throw new IllegalArgumentException("User admin or user group SuperUsers does't exists");
        }
        personManagementServiceDao.addPersonToGroup(superUsersGroupId, adminId);
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
        String query = "select count(*) from \"" + STATUS_DO + "\" s where s.\"name\"=:name";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("name", statusName);
        @SuppressWarnings("deprecation")
        int total = jdbcTemplate.queryForObject(query, paramMap, Integer.class);
        return total > 0;
    }

    private boolean existsStaticGroup(String staticGroupName) {
        String query = "select count(*) from \"user_group\" ug where ug.\"group_name\" = :group_name and ug.\"object_id\" is null";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("group_name", staticGroupName);
        int total = jdbcTemplate.queryForObject(query, paramMap, Integer.class);
        return total > 0;
    }

    private void saveStatus(String statusName) {
        GenericDomainObject statusDO = new GenericDomainObject();
        statusDO.setTypeName(STATUS_DO);
        Date currentDate = new Date();
        statusDO.setCreatedDate(currentDate);
        statusDO.setModifiedDate(currentDate);
        statusDO.setString("Name", statusName);
        AccessToken accessToken = accessControlService.createSystemAccessToken("InitialDataLoader");
        domainObjectDao.save(statusDO, accessToken);
    }

    private void insertSuperUserAuthenticationInfo() {
        AuthenticationInfoAndRole superUser = new AuthenticationInfoAndRole();
        superUser.setUserUid(ADMIN_LOGIN);
        superUser.setPassword(ADMIN_PSSWRD);
        superUser.setRole("admin");
        
        Id superUsersGroupId = personManagementServiceDao.getGroupId(GenericDomainObject.SUPER_USERS_STATIC_GROUP);

        authenticationService.insertAuthenticationInfoAndRole(superUser, superUsersGroupId);
    }

    private void insertAdministratorAuthenticationInfo() {
        Id administratorsGroupId = personManagementServiceDao.getGroupId(GenericDomainObject.ADMINISTRATORS_STATIC_GROUP);

        AuthenticationInfoAndRole administratorAuthInfo = new AuthenticationInfoAndRole();
        administratorAuthInfo.setUserUid(GenericDomainObject.ADMINISTRATOR_LOGIN);
        administratorAuthInfo.setPassword(GenericDomainObject.ADMINISTRATOR_PSSWRD);
        administratorAuthInfo.setRole("admin");
      
        authenticationService.insertAuthenticationInfoAndRole(administratorAuthInfo, administratorsGroupId);
    }

}
