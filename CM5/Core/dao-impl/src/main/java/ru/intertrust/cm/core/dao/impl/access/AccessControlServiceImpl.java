package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.exception.AccessException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Реализация службы контроля доступа.
 * <p>Объект типа AccessControlServiceImpl создаётся через Spring-контекст приложения (beans.xml).
 * Этим же способом с ним обычно связываются другие службы, входящие в систему.
 * <p>Для функционирования службы необходимо связать её с агентом БД по запросам прав доступа
 * (см. {{@link #setDatabaseAgent(DatabaseAccessAgent)}. Обычно это делается также через Spring-контекст приложения.
 * 
 * @author apirozhkov
 */
public class AccessControlServiceImpl implements AccessControlService {

    @Autowired
    private DatabaseAccessAgent databaseAgent;

    @Autowired    
    private PersonManagementServiceDao personManagementService;
        
    private Map<String, Id> loginToUserIdCache = new HashMap<String, Id>();
    
    private Map<Id, Boolean> personIdToIsSuperUserCache = new HashMap<Id, Boolean>();
    
    private Id superUsersGroupId = null;
    
    /**
     * Устанавливает программный агент, которому делегируются функции физической проверки прав доступа
     * через запросы в БД. 
     * 
     * @param databaseAgent Агент БД по запросам прав доступа в БД
     */
    public void setDatabaseAgent(DatabaseAccessAgent databaseAgent) {
        this.databaseAgent = databaseAgent;
    }    

    public void setPersonManagementService(PersonManagementServiceDao personManagementService) {
        this.personManagementService = personManagementService;
    }

    @Override
    public AccessToken createSystemAccessToken(String processId) {
        AccessToken token = new UniversalAccessToken(new SystemSubject(processId));
        return token;
    }

    @Override
    public AccessToken createAdminAccessToken(String login) throws AccessException {
        Id personId = getUserIdByLogin(login);
        return null;
    }

    @Override
    public AccessToken createAccessToken(String login, Id objectId, AccessType type)
            throws AccessException {
        
        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }       

        boolean deferred = false;
        if (DomainObjectAccessType.READ.equals(type)) {
            deferred = true; // Проверка прав на чтение объекта осуществляется при его выборке
        } else { // Для всех других типов доступа к доменному объекту производим запрос в БД

            if (!databaseAgent.checkDomainObjectAccess(personIdInt, objectId, type)) {
                throw new AccessException();
            }
        }
        
        AccessToken token = new SimpleAccessToken(new UserSubject(personIdInt), objectId, type, deferred);
        return token;
    }

    private Id getUserIdByLogin(String login) {
        Id personId = null;
        if (loginToUserIdCache.get(login) != null) {
            personId = loginToUserIdCache.get(login);
        } else {
            personId = personManagementService.getPersonId(login);
            loginToUserIdCache.put(login, personId);
        }        
        return personId;
    }

    
    private Id getSuperUsersGroupId() {
        if (superUsersGroupId == null) {
            superUsersGroupId = personManagementService.getGroupId(GenericDomainObject.SUPER_USERS_STATIC_GROUP);
        }
        return superUsersGroupId;
    }

    private boolean isPersonSuperUser(Id personId) {
        boolean isSuperUser = false;
        if (personIdToIsSuperUserCache.get(personId) != null) {
            isSuperUser = personIdToIsSuperUserCache.get(personId);
        } else {
            isSuperUser = personManagementService.isPersonInGroup(getSuperUsersGroupId(), personId);
            personIdToIsSuperUserCache.put(personId, isSuperUser);
        }
        return isSuperUser;

    }
    
    @Override
    public AccessToken createCollectionAccessToken(String login) throws AccessException {
        boolean deferred = true;

        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();

        boolean isSuperUser = isPersonSuperUser(personId);
        if (isSuperUser) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }
      
        AccessToken token =
                new SimpleAccessToken(new UserSubject(personIdInt), null, DomainObjectAccessType.READ, deferred);
        return token;
    }

    @Override
    public AccessToken createAccessToken(String login, Id[] objectIds, AccessType type, boolean requireAll)
            throws AccessException {
        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        Id[] ids = databaseAgent.checkMultiDomainObjectAccess(personIdInt, objectIds, type);
        if (requireAll ? ids.length < objectIds.length : ids.length == 0) {
            throw new AccessException();
        }
        AccessToken token = new MultiObjectAccessToken(new UserSubject(personIdInt), ids, type);

        return token;
    }

    @Override
    public AccessToken createAccessToken(String login, Id objectId, AccessType[] types, boolean requireAll)
            throws AccessException {

        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        AccessType[] granted = databaseAgent.checkDomainObjectMultiAccess(personIdInt, objectId, types); if (requireAll ?
        granted.length < types.length : granted.length == 0) { throw new AccessException(); }

        
        AccessToken token = new MultiTypeAccessToken(new UserSubject(personIdInt), objectId, types);
        return token;
    }

    @Override
    public void verifyAccessToken(AccessToken token, Id objectId, AccessType type) throws AccessException {
        AccessTokenBase trustedToken;
        try {
            trustedToken = (AccessTokenBase) token;
        } catch (ClassCastException e) {
            throw new AccessException("Fake access token");
        }
        if (!trustedToken.isOriginalFor(this)) {
            throw new AccessException("Fake access token");
        }

        if (!trustedToken.allowsAccess(objectId, type)) {
            throw new AccessException("Wrong access token");
        }
    }

    @Override
    public void verifySystemAccessToken(AccessToken accessToken) throws AccessException {    
        
        AccessTokenBase trustedToken;       
        try {
            trustedToken = (AccessTokenBase) accessToken;
        } catch (ClassCastException e) {
            throw new AccessException("Fake access token");
        }
        
        if (!trustedToken.isOriginalFor(this)) {
            throw new AccessException("Fake access token");
        }
        if (accessToken == null || !accessToken.getClass().equals(UniversalAccessToken.class)) {
            throw new AccessException("Not valid system access token");
        }

    }
    
    public void cleanPersonCache(){
        personIdToIsSuperUserCache.clear();
        loginToUserIdCache.clear();
        
    }
    
    /**
     * Базовый класс для маркеров доступа.
     * Все маркеры доступа, поддерживаемые данной реализацией сервиса контроля доступа,
     * наследуются от этого класса.
     */
    private abstract class AccessTokenBase implements AccessToken {

        private AccessControlServiceImpl origin = AccessControlServiceImpl.this;

        /**
         * Проверяет тот факт, что маркер доступа был создан запрошенным экземпляром службы.
         * Этот метод используется 
         * 
         * @param service Экземпляр службы контроля доступа для проверки
         * @return true если маркер доступа был создан тем же экземпляром службы
         */
        boolean isOriginalFor(AccessControlServiceImpl service) {
            return origin == service;   // Сравниваем не по equals, т.к. должен быть именно один и тот же экземпляр
        }

        /**
         * Определяет, соответствует ли данный маркер запрошенному доступу к запрошенному объекту.
         * 
         * @param objectId Идентификатор доменного объекта
         * @param type Тип доступа
         * @return true если маркер соответствует запрошенному доступу
         */
        abstract boolean allowsAccess(Id objectId, AccessType type);
    }

    /**
     * Маркер доступа для простых операций с доменными объектами &mdash; чтения, изменения и удаления.
     * Поддерживают опцию отложенности.
     */
    final class SimpleAccessToken extends AccessTokenBase {

        private final UserSubject subject;
        private final Id objectId;
        private final AccessType type;
        private final boolean deferred;

        SimpleAccessToken(UserSubject subject, Id objectId, AccessType type, boolean deferred) {
            this.subject = subject;
            this.objectId = objectId;
            this.type = type;
            this.deferred = deferred;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public boolean isDeferred() {
            return deferred;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return this.objectId.equals(objectId) && this.type.equals(type);
        }
    }

    /**
     * Маркер доступа к набору доменных объектов. Задаёт разрешение на определённый тип доступа
     * сразу к множеству объектов. Не может быть отложенным.
     */
    private final class MultiObjectAccessToken extends AccessTokenBase {

        private final UserSubject subject;
        private final Set<Id> objectIds;
        private final AccessType type;

        MultiObjectAccessToken(UserSubject subject, Id[] objectIds, AccessType type) {
            this.subject = subject;
            this.objectIds = new HashSet<>(objectIds.length);
            this.objectIds.addAll(Arrays.asList(objectIds));
            this.type = type;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return this.objectIds.contains(objectId) && this.type.equals(type);
        }
    }

    /**
     * Маркер множественных типов доступа к доменному объекту.
     * Не может быть отложенным.
     */
    private final class MultiTypeAccessToken extends AccessTokenBase {

        private final UserSubject subject;
        private final Id objectId;
        private final Set<AccessType> types;

        MultiTypeAccessToken(UserSubject subject, Id objectId, AccessType[] types) {
            this.subject = subject;
            this.objectId = objectId;
            this.types = new HashSet<>(types.length);
            this.types.addAll(Arrays.asList(types));
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return this.objectId.equals(objectId) && this.types.contains(type);
        }
    }

    /**
     * Универсальный маркер доступа. Разрешает любой доступ к любому объекту.
     * Предоставляется только системным субъектам &mdash; процессам, работающим от имени системы.
     */
    private final class UniversalAccessToken extends AccessTokenBase {

        private final SystemSubject subject;

        UniversalAccessToken(SystemSubject subject) {
            this.subject = subject;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return true;    // Разрешает любой доступ к любому объекту
        }
    }
    
    /**
     * Маркер доступа для суперпользователя. Разрешает любой доступ к любому объекту.
     * Отличается от системного тем, что хранит идентификатор пользователя (необходим для аудит логов).
     * @author atsvetkov
     *
     */
    private final class SuperUserAccessToken extends AccessTokenBase {

        private final UserSubject subject;

        SuperUserAccessToken(UserSubject subject) {
            this.subject = subject;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return true; // Разрешает любой доступ к любому объекту
        }
    }
}
