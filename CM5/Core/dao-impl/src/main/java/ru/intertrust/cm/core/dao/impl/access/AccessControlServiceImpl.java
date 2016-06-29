package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.model.AccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    PermissionServiceDao permissionServiceDao;

    @Autowired
    DynamicGroupService dynamicGroupService;
    
    @Autowired
    private UserGroupGlobalCache userGroupCache;
    
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private EventLogService eventLogService;

    /**
     * Устанавливает программный агент, которому делегируются функции физической проверки прав доступа
     * через запросы в БД. 
     * 
     * @param databaseAgent Агент БД по запросам прав доступа в БД
     */
    public void setDatabaseAgent(DatabaseAccessAgent databaseAgent) {
        this.databaseAgent = databaseAgent;
    }    

    public void setUserGroupCache(UserGroupGlobalCache userGroupCache) {
        this.userGroupCache = userGroupCache;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
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
        return createAccessToken(login, objectId, type, true);
    }

    public boolean verifyAccess(String login, Id objectId, AccessType type) {
        try {
            createAccessToken(login, objectId, type, false);
            return true;
        } catch (AccessException e) {
            return false;
        }
    }

    private AccessToken createAccessToken(String login, Id objectId, AccessType type, boolean log) throws AccessException {

        final Id personId = getUserIdByLogin(login);
        final int personIdInt = (int) ((RdbmsId) personId).getId();
        final boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser || isAdministratorWithAllPermissions(personId, objectId)) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        boolean deferred = false;
        if (DomainObjectAccessType.READ.equals(type)) {
            deferred = true; // Проверка прав на чтение объекта осуществляется при его выборке
        } else { // Для всех других типов доступа к доменному объекту производим запрос в БД
            
            objectId = getRelevantObjectId(objectId);
            if (!databaseAgent.checkDomainObjectAccess(personIdInt, objectId, type)) {
                if (log) {
                    eventLogService.logAccessDomainObjectEvent(objectId, EventLogService.ACCESS_OBJECT_WRITE, false);
                }
                String message = String.format(
                        MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_FOR_DO, GuiContext.getUserLocale()),
                        login,type,objectId);
                throw new AccessException(message);
            }
        }

        AccessToken token = new SimpleAccessToken(new UserSubject(personIdInt), objectId, type, deferred);

        if (log && (DomainObjectAccessType.WRITE.equals(type) || DomainObjectAccessType.DELETE.equals(type))) {
            eventLogService.logAccessDomainObjectEvent(objectId, EventLogService.ACCESS_OBJECT_WRITE, true);
        }
        return token;
    }

    /**
     * Если пользователь входит в группу {@link GenericDomainObject#ADMINISTRATORS_STATIC_GROUP} и для ДО не установлена
     * матрица доступа, то пользователь имеет все права на данный ДО.
     * @param personId
     * @param objectId
     * @return
     */
    private boolean isAdministratorWithAllPermissions(Id personId, Id objectId) {
        return userGroupCache.isAdministrator(personId) && ! hasAccessMatrix(objectId);
    }
    
    private boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        return userGroupCache.isAdministrator(personId) && configurationExplorer.getAccessMatrixByObjectType(domainObjectType) == null;
    }

    private boolean hasAccessMatrix(Id objectId) {
        if(objectId == null){
            return false;
        }
        String domainObjectType = domainObjectTypeIdCache.getName(objectId);
        AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectType(domainObjectType);
        return accessMatrix != null;
    }

    private Id getRelevantObjectId(Id id) {
        if (id == null) {
            return null;
        }
        Id objectId = id;
        String domainObjectType = domainObjectTypeIdCache.getName(objectId);
        if (configurationExplorer.isAuditLogType(domainObjectType)) {
            AccessToken systemAccessToken = createSystemAccessToken("AccessControlServiceImpl");
            DomainObject parentDomainObject = domainObjectDao.find(objectId, systemAccessToken);
            objectId = parentDomainObject.getReference(Configuration.DOMAIN_OBJECT_ID_COLUMN);
        }
        return objectId;
    }
    
    @Override
    public AccessToken createDomainObjectCreateToken(String login, String objectType, Id[] parentObjects)
            throws AccessException {

        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();

        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser || isAdministratorWithAllPermissions(personId, objectType)) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        if (parentObjects != null && parentObjects.length > 0) {
            AccessType accessType = new CreateChildAccessType(objectType);
            return createAccessToken(login, parentObjects, accessType, true);
        }

        if (isAllowedToCreateByStaticGroups(personId, objectType)) {
            List<String> parentTypes = Arrays.asList(configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(objectType));

            AccessType accessType = new CreateObjectAccessType(objectType, parentTypes);
            return new SimpleAccessToken(new UserSubject(personIdInt), null, accessType, false);
        }
        String message = String.format(
                MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_CR_NOT_ALWD, GuiContext.getUserLocale()),
                objectType,login);
        throw new AccessException(message);
   }

    /**
     * Проверяет права на создание ДО, данные контексным динамическим группам (ролям) и безконтекстным группам.
     * Права для контексных групп настраиваются через <create-child> разрешение у родительского типа.
     * Например,
     *     <create-child type="address">
     *          <permit-role name="Contact_Name_Editor_Role" />
     *     </create-child>
     * 
     * Права для статических и безконтекстных групп настраиваются через <create> тег.
     * Например,
     *     <create>
     *          <permit-group name="AllPersons" />
     *     </create>
     * При этом учитываются права на создание, данные через косвенные матрицы доступа. Тип ДО косвенной матрицы доступа 
     * определяется по типу ссылочного поля. 
     */
    
    @Override
    public AccessToken createDomainObjectCreateToken(String login, DomainObject domainObject)
            throws AccessException {
        List<ImmutableFieldData> immutableFieldDataList = AccessControlUtility.getImmutableParentIds(domainObject, domainObject.getTypeName(), configurationExplorer); 
        String objectType = domainObject.getTypeName();
        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();

        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser || isAdministratorWithAllPermissions(personId, objectType)) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        if (immutableFieldDataList != null && immutableFieldDataList.size() > 0) {
            Id[] parentObjects = new Id[immutableFieldDataList.size()];
            AccessType[] types = new AccessType[immutableFieldDataList.size()]; 
            for (int i=0; i<immutableFieldDataList.size(); i++) {
                parentObjects[i] = immutableFieldDataList.get(i).getValue();
                types[i] = new CreateChildAccessType(immutableFieldDataList.get(i).getTypeName());                 
            }
            
            AccessToken result = null;
            // CMFIVE-5892 В большинстве случаев права мы получим при первом же вызове, но если нет прав 
            // то получим ошибку затем пересчитываем права у тех ДО к которым устанавливаем связь и еще раз пробуем получить токен
            try{
                //Первая попытка получить accessTocken
                result = createAccessToken(login, parentObjects, types, true);
            }catch(AccessException ex){
                //Может не быть прав на создание, потому что еще не пересчитаны права на ДО куда ссылаются immutable поля
                Set<Id> idsForRecalc = new HashSet<Id>();
                for (Id parentObjectId : parentObjects) {
                    idsForRecalc.add(parentObjectId);
                }
                permissionServiceDao.refreshAclIfMarked(idsForRecalc);
                //Повторная попытка получить accessTocken
                result = createAccessToken(login, parentObjects, types, true);
            }
                        
            return result;
        }

        if (isAllowedToCreateByStaticGroups(personId, domainObject)) {
            List<String> parentTypes = Arrays.asList(configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(objectType));

            AccessType accessType = new CreateObjectAccessType(objectType, parentTypes);
            return new SimpleAccessToken(new UserSubject(personIdInt), null, accessType, false);
        }
        String message = String.format(
                MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_CR_NOT_ALWD, GuiContext.getUserLocale()),
                objectType,login);

        throw new AccessException(message);

    }

    private boolean isAllowedToCreateByStaticGroups(Id userId, String objectType) {
        return databaseAgent.isAllowedToCreateByStaticGroups(userId, objectType);
    }

    private boolean isAllowedToCreateByStaticGroups(Id userId, DomainObject domainObject){
        return databaseAgent.isAllowedToCreateByStaticGroups(userId, domainObject);
    }

    private Id getUserIdByLogin(String login) {
        return userGroupCache.getUserIdByLogin(login);
    }

    private boolean isPersonSuperUser(Id personId) {
        return userGroupCache.isPersonSuperUser(personId);
    }
    
    @Override
    public AccessToken createCollectionAccessToken(String login) throws AccessException {

        final Id personId = getUserIdByLogin(login);
        final Integer personIdInt = (int) ((RdbmsId) personId).getId();

        final boolean isSuperUser = isPersonSuperUser(personId);
        if (isSuperUser) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        return new SimpleAccessToken(new UserSubject(personIdInt), null, DomainObjectAccessType.READ, true);
    }

    @Override
    public AccessToken createAccessToken(String login, Id[] objectIds, AccessType type, boolean requireAll)
            throws AccessException {
        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser ) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        Id[] ids;
        boolean deferred = false;
        AccessToken token;
        
        if (DomainObjectAccessType.READ.equals(type)) {
            deferred = true;
            token = new MultiObjectAccessToken(new UserSubject(personIdInt), objectIds, type, deferred);
        } else {
            ids = databaseAgent.checkMultiDomainObjectAccess(personIdInt, objectIds, type);
            if (requireAll ? ids.length < objectIds.length : ids.length == 0) {
                String message = String.format(
                        MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_FOR_DO, GuiContext.getUserLocale()),
                        login,type,objectIds.toString());
                String childType = "";
                if (type instanceof CreateChildAccessType) {
                    childType = ((CreateChildAccessType) type).getChildType();
                    message = String.format(
                            MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_FOR_CHILD, GuiContext.getUserLocale()),
                            login,type,childType);
                }
                throw new AccessException(message);
            }
            token = new MultiObjectAccessToken(new UserSubject(personIdInt), ids, type, deferred);   
        }

        return token;
    }

    @Override
    public AccessToken createAccessToken(String login, Id[] objectIds, AccessType[] types, boolean requireAll)
            throws AccessException {
        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser ) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        List<Id> ids = new ArrayList<Id>();
        List<AccessType> accessTypes = new ArrayList<AccessType>();
        boolean deferred = false;
        AccessToken token;
        
        for (int i=0; i<objectIds.length; i++) {
            if (databaseAgent.checkDomainObjectAccess(personIdInt, objectIds[i], types[i])){
                ids.add(objectIds[i]);
                accessTypes.add(types[i]);
            }            
        }
        
        if (requireAll ? ids.size() < objectIds.length : ids.size() == 0) {
            String message = String.format(
                    MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_FOR_DO, GuiContext.getUserLocale()),
                    login, types.toString(), objectIds.toString());
            String childType = "";
            if (types[0] instanceof CreateChildAccessType) {
                for (AccessType type : types) {
                    childType += ((CreateChildAccessType) type).getChildType() + ", ";
                }
                
                message = String.format(
                        MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_NO_PERMISSIONS_FOR_CHILD, GuiContext.getUserLocale()),
                        login,types[0],childType);
            }
            throw new AccessException(message);
        }
        token = new MultyTypeMultiObjectAccessToken(
                new UserSubject(personIdInt), 
                ids.toArray(new Id[ids.size()]), 
                accessTypes.toArray(new AccessType[accessTypes.size()]), 
                deferred);   

        return token;
    }
    
    
    @Override
    public AccessToken createAccessToken(String login, Id objectId, AccessType[] types, boolean requireAll)
            throws AccessException {

        Id personId = getUserIdByLogin(login);
        Integer personIdInt = (int) ((RdbmsId) personId).getId();
        boolean isSuperUser = isPersonSuperUser(personId);

        if (isSuperUser || isAdministratorWithAllPermissions(personId, objectId)) {
            return new SuperUserAccessToken(new UserSubject(personIdInt));
        }

        AccessType[] granted = databaseAgent.checkDomainObjectMultiAccess(personIdInt, objectId, types);
        if (requireAll ?
                granted.length < types.length : granted.length == 0) {
            throw new AccessException();
        }
        
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
            throw new AccessException(MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_WRONG_ACCESS_TOKEN, GuiContext.getUserLocale()));
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
            throw new AccessException(MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_WRONG_SYSTEM_ACCESS_TOKEN, GuiContext.getUserLocale()));
        }

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

        @Override
        public AccessLimitationType getAccessLimitationType() {
            return AccessLimitationType.LIMITED;
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
            if (this.objectId == null || objectId == null) {
                if (this.type instanceof CreateObjectAccessType && type instanceof CreateObjectAccessType) {
                    CreateObjectAccessType originalAccessType = (CreateObjectAccessType) this.type;
                    CreateObjectAccessType checkAccessType = (CreateObjectAccessType) type;
                    if (originalAccessType.getObjectType().equals(checkAccessType.getObjectType())) {
                        return true;
                    } else if (originalAccessType.getParentTypes().contains(checkAccessType.getObjectType())) {
                        return true;
                    }
                }
                return this.type.equals(type);
            } else {
                return this.objectId.equals(objectId) && this.type.equals(type);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleAccessToken that = (SimpleAccessToken) o;

            if (deferred != that.deferred) return false;
            if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null) return false;
            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;
            if (type != null ? !type.equals(that.type) : that.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject != null ? subject.hashCode() : 0;
            result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (deferred ? 1 : 0);
            return result;
        }
    }

    
    /**
     * Маркер доступа на создание доменных объектов
     * @author atsvetkov
     */
    final class DomainObjectCreateToken extends AccessTokenBase {

        private final UserSubject subject;
        private final String objectType;

        DomainObjectCreateToken(UserSubject subject, String objectType) {
            this.subject = subject;
            this.objectType = objectType;
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
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DomainObjectCreateToken that = (DomainObjectCreateToken) o;

            if (objectType != null ? !objectType.equals(that.objectType) : that.objectType != null) return false;
            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject != null ? subject.hashCode() : 0;
            result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
            return result;
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
        private final boolean deferred;

        MultiObjectAccessToken(UserSubject subject, Id[] objectIds, AccessType type, boolean deferred) {
            this.subject = subject;
            this.objectIds = new HashSet<>((int) (objectIds.length / 0.75 + 1));
            this.objectIds.addAll(Arrays.asList(objectIds));
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
            return this.objectIds.contains(objectId) && this.type.equals(type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MultiObjectAccessToken that = (MultiObjectAccessToken) o;

            if (deferred != that.deferred) return false;

            if (objectIds != null) {
                if (that.objectIds == null) {
                    return false;
                } else if (objectIds.size() != that.objectIds.size() || !objectIds.containsAll(that.objectIds)) {
                    return false;
                }
            } else if (that.objectIds != null) {
                return false;
            }

            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;
            if (type != null ? !type.equals(that.type) : that.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject != null ? subject.hashCode() : 0;

            if (objectIds != null) {
                for (Id id : objectIds) {
                    if (id != null) {
                        result = result + id.hashCode();
                    }
                }
            }

            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (deferred ? 1 : 0);
            return result;
        }
    }

    /**
     * Маркер доступа к набору доменных объектов. Задаёт разрешение разные типы доступа
     * сразу к множеству объектов. Не может быть отложенным.
     */
    private final class MultyTypeMultiObjectAccessToken extends AccessTokenBase {

        private final UserSubject subject;
        private final Set<Id> objectIds;
        private final Set<AccessType> types;
        private final boolean deferred;

        MultyTypeMultiObjectAccessToken(UserSubject subject, Id[] objectIds, AccessType[] types, boolean deferred) {
            this.subject = subject;
            this.objectIds = new HashSet<>((int) (objectIds.length / 0.75 + 1));
            this.objectIds.addAll(Arrays.asList(objectIds));
            this.types = new HashSet<AccessType>();
            this.types.addAll(Arrays.asList(types));
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
            return this.objectIds.contains(objectId) && this.types.contains(type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MultyTypeMultiObjectAccessToken that = (MultyTypeMultiObjectAccessToken) o;

            if (deferred != that.deferred) return false;

            if (objectIds != null) {
                if (that.objectIds == null) {
                    return false;
                } else if (objectIds.size() != that.objectIds.size() || !objectIds.containsAll(that.objectIds)) {
                    return false;
                }
            } else if (that.objectIds != null) {
                return false;
            }

            if (types != null) {
                if (that.types == null) {
                    return false;
                } else if (types.size() != that.types.size() || !types.containsAll(that.types)) {
                    return false;
                }
            } else if (that.types != null) {
                return false;
            }
            
            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject != null ? subject.hashCode() : 0;

            if (objectIds != null) {
                for (Id id : objectIds) {
                    if (id != null) {
                        result = result + id.hashCode();
                    }
                }
            }
            if (types != null) {
                for (AccessType type : types) {
                    if (type != null) {
                        result = result + type.hashCode();
                    }
                }
            }

            result = 31 * result + (deferred ? 1 : 0);
            return result;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MultiTypeAccessToken that = (MultiTypeAccessToken) o;

            if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null) return false;
            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

            if (types != null) {
                if (that.types == null) {
                    return false;
                } else if (types.size() != that.types.size() || !types.containsAll(that.types)) {
                    return false;
                }
            } else if (that.types != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject != null ? subject.hashCode() : 0;
            result = 31 * result + (objectId != null ? objectId.hashCode() : 0);

            if (types != null) {
                for (AccessType accessType : types) {
                    if (accessType != null) {
                        result = result + accessType.hashCode();
                    }
                }
            }

            return result;
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
        public AccessLimitationType getAccessLimitationType() {
            return AccessLimitationType.UNLIMITED;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return true;    // Разрешает любой доступ к любому объекту
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UniversalAccessToken that = (UniversalAccessToken) o;

            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return subject != null ? subject.hashCode() : 0;
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
        public AccessLimitationType getAccessLimitationType() {
            return AccessLimitationType.UNLIMITED;
        }

        @Override
        boolean allowsAccess(Id objectId, AccessType type) {
            return true; // Разрешает любой доступ к любому объекту
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SuperUserAccessToken that = (SuperUserAccessToken) o;

            if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return subject != null ? subject.hashCode() : 0;
        }
    }

    @Override
    public void verifyDeferredAccessToken(AccessToken token, Id objectId, AccessType type) throws AccessException {
        if (token.isDeferred()) {
            UserSubject subject = getUserSubject(token);
            Integer personIdInt = null;
            Id personId = null;
            if (subject != null) {
                personIdInt = subject.getUserId();
                personId = new RdbmsId(domainObjectTypeIdCache.getId(GenericDomainObject.PERSON_DOMAIN_OBJECT), personIdInt);

            } else {
                personId = currentUserAccessor.getCurrentUserId();
                personIdInt = (int) (((RdbmsId) personId).getId());
            }

            boolean isSuperUser = isPersonSuperUser(personId);

            if (isSuperUser || isAdministratorWithAllPermissions(personId, objectId)) {
                return;
            }

            if (DomainObjectAccessType.READ.equals(type)) {
                String doType = domainObjectTypeIdCache.getName(objectId);
                if (!configurationExplorer.isReadPermittedToEverybody(doType) &&
                        !databaseAgent.checkDomainObjectReadAccess(personIdInt, objectId)) {
                    String message = String.format(
                            MessageResourceProvider.getMessage(LocalizationKeys.ACL_SERVICE_READ_PERMISSIONS_DENIED, GuiContext.getUserLocale()),
                            objectId,personId);
                    throw new AccessException(message);
                }

            }
        }
    }

    private UserSubject getUserSubject(AccessToken token) {
        UserSubject subject = null;

        if (token instanceof SimpleAccessToken) {
            SimpleAccessToken simpleToken = (SimpleAccessToken) token;
            subject = (UserSubject) simpleToken.getSubject();
        }
        return subject;
    }

    @Override
    public void verifyAccessTokenOnCreate(AccessToken token, DomainObject domainObject, Integer type) {
        String domainObjectType = domainObjectTypeIdCache.getName(type);
        List<ImmutableFieldData> parentIds = AccessControlUtility.getImmutableParentIds(domainObject, domainObjectType, configurationExplorer);
        
        if (parentIds != null && parentIds.size() > 0) {
            //Не проверяем систмный и админский токен
            if (!(token instanceof UniversalAccessToken || token instanceof SuperUserAccessToken)){
                String currentUser = currentUserAccessor.getCurrentUser();
                for (ImmutableFieldData immutableFieldDataList : parentIds) {
                    Id parentId = immutableFieldDataList.getValue();
                    //При формирование CreateChildAccessType нужно передавать имя того типа, reference на который указан в настройке поля. 
                    //Непосредственно domainObjectType передавать нельзя, так как зачастую нужно передать имя родительского типа
                    AccessType accessType = new CreateChildAccessType(immutableFieldDataList.getTypeName());
                    
                    AccessToken linkAccessToken = createAccessToken(currentUser, parentId, accessType);
                    verifyAccessToken(linkAccessToken, parentId, accessType);
                }
            }
        } else {

            AccessType accessType = new CreateObjectAccessType(domainObjectType, null);
            verifyAccessToken(token, null, accessType);
        }        
    }
}
