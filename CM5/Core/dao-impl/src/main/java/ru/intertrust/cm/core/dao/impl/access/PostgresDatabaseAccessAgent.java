package ru.intertrust.cm.core.dao.impl.access;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.AccessMatrixConfig.BorrowPermissisonsMode;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextConfig;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.IdSorterByType;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация агента БД по запросам прав доступа для PostgreSQL.
 * <p>
 * Перед использованием объекта необходимо установить корректный источник данных
 * вызовом метода {@link #setDataSource(DataSource)}. Обычно это делается через
 * соотвествующий атрибут или тег конфигурации объекта в Spring-контексте
 * приложения (beans.xml), где создаётся и сам объект.
 * 
 * @author apirozhkov
 */
public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {
    private static final Logger logger = LoggerFactory.getLogger(PostgresDatabaseAccessAgent.class);

    private static final String ALL_PERSONS_GROUP = "*";

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private UserGroupGlobalCache userGroupCache;

    @Autowired
    private DomainObjectQueryHelper domainObjectQueryHelper;

    @Autowired
    private AccessControlConverter accessControlConverter;

    @Autowired
    private PermissionServiceDao permissionServiceDao;

    private NamedParameterJdbcTemplate jdbcTemplate;

    private CaseInsensitiveMap<AccessMatrixConfig> accessMatrixConfigMap = new CaseInsensitiveMap<>();
    private CaseInsensitiveMap<DomainObjectTypeConfig> childDomainObjectTypeMap = new CaseInsensitiveMap<>();

    public void setDomainObjetcTypeIdCache(DomainObjectTypeIdCache domainObjetcTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjetcTypeIdCache;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Устанавливает источник данных, который будет использоваться для
     * выполнения запросов.
     * 
     * @param dataSource
     *            Источник данных
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type) {

        RdbmsId id = (RdbmsId) objectId;
        String doType = domainObjectTypeIdCache.getName(objectId);
        //В случае наличия заимствования прав меняется проверяемый тип доступа
        List<AccessType> checkAccessType = getMatrixReferencePermission(doType, type);

        List<String> opCode = new ArrayList<String>();
        boolean opRead = false;
        boolean opReadAttach = false;
        for (AccessType accessType : checkAccessType) {
            //Особым образом обрабатываем READ, так как права на чтение хранятся в другой таблице
            if (accessType.equals(DomainObjectAccessType.READ)) {
                opRead = true;
            } else if (DomainObjectAccessType.READ_ATTACH.equals(type)) {
                opReadAttach = true;
            } else {
                opCode.add(makeAccessTypeCode(accessType));
            }
        }

        boolean result = false;
        if (opRead) {
            result = configurationExplorer.isReadPermittedToEverybody(doType);
            if (!result) {
                result = checkDomainObjectReadAccess(userId, objectId);
            }
        } else if (opReadAttach) {
            result = checkAttachReadContentAccess(userId, objectId);
        } else {
            if (opCode.size() > 0) {
                String query = getQueryForCheckDomainObjectAccess(id);
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("user_id", userId);
                parameters.put("object_id", id.getId());
                parameters.put("operation", opCode);
                Integer count = jdbcTemplate.queryForObject(query, parameters, Integer.class);
                result = count > 0;
            }
        }
        return result;
    }

    /**
     * Проверка прав на чтение содержимого
     *
     * @param userId id пользователя
     * @param attachId id вложения
     * @return true, если есть права
     */
    private boolean checkAttachReadContentAccess(int userId, Id attachId) {

        RdbmsId rdbmsAttachId = (RdbmsId) attachId;
        String objectType = domainObjectTypeIdCache.getName(rdbmsAttachId.getTypeId());
        if (!configurationExplorer.isAttachmentType(objectType)) {
            throw new IllegalArgumentException();
        }

        if (!permissionServiceDao.checkIfContentReadRestricted(attachId)) {
            return true;
        }

        String domainObjectAclTable = getAclTableName(rdbmsAttachId);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTypeIdCache.getName(rdbmsAttachId.getTypeId())));

        String query = getQueryForCheckAccessInAclTable(domainObjectAclTable, domainObjectBaseTable);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
        parameters.put("object_id", rdbmsAttachId.getId());
        parameters.put("operation", AccessControlConverter.OP_READ_ATTACH);
        Integer count = jdbcTemplate.queryForObject(query, parameters, Integer.class);

        return count > 0;
    }

    @Override
    public boolean checkDomainObjectReadAccess(int userId, Id objectId) {
        RdbmsId id = (RdbmsId) objectId;

        String query = getQueryForCheckDomainObjectReadAccess(id);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_id", id.getId());
        Integer result = jdbcTemplate.queryForObject(query, parameters, Integer.class);
        return result > 0;
    }

    /**
     * Метод получает права, которые необходимо проверить с учетом мапинга прав
     * в случае заимствования прав
     * @param typeName
     *            права которые проверяются
     * @return права которые необходимо проверить на данных у типа, права
     *         которого заимствуются
     */
    @Override
    public List<AccessType> getMatrixReferencePermission(String typeName, AccessType accessType) {

        if (DomainObjectAccessType.READ_ATTACH.equals(accessType)) {
            return Collections.singletonList(accessType);
        }

        List<AccessType> result = new ArrayList<AccessType>();
        //Проверяем нет ли заимствования прав и в случае наличия подменяем тип доступа согласно мапингу
        String martixRef = configurationExplorer.getMatrixReferenceTypeName(typeName);

        final AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(typeName);

        //Флаг комбинированного заимствования прав, когда права на чтение заимствуются, а на запись и удаления настраиваются собственные
        boolean combinateAccessReference = AccessControlUtility.isCombineMatrixReference(accessMatrix);

        //Маппинг производится только тогда когда есть заимснвование прав и эаимствование не комбинированное
        if (martixRef != null && !combinateAccessReference) {
            //Получаем маппинг прав
            AccessMatrixConfig matrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(typeName);
            if (matrix.getMatrixReferenceMappingConfig() != null) {
                //Берем мапинг из матрицы
                MatrixReferenceMappingConfig mapping = matrix.getMatrixReferenceMappingConfig();
                if (mapping != null) {
                    for (MatrixReferenceMappingPermissionConfig mappingPermissions : mapping.getPermission()) {
                        if (isMappingToAccessType(mappingPermissions, accessType)) {
                            List<AccessType> mappedAccessType = getAccessTypeFromMapping(martixRef, mappingPermissions);
                            result.addAll(mappedAccessType);
                        }
                    }
                }
            } else {
                //Маппинг по умолчанию read->read, write->write+delete, delete->delete
                result.add(accessType);
                if (accessType.equals(DomainObjectAccessType.DELETE)) {
                    result.add(DomainObjectAccessType.WRITE);
                }
            }
        } else {
            result.add(accessType);
        }
        return result;
    }

    private List<AccessType> getAccessTypeFromMapping(String typeName, MatrixReferenceMappingPermissionConfig mappingPermissions) {
        List<AccessType> result = new ArrayList<AccessType>();

        if (mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.READ)) {
            result.add(DomainObjectAccessType.READ);
        } else if (mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.WRITE)) {
            result.add(DomainObjectAccessType.WRITE);
        } else if (mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.DELETE)) {
            result.add(DomainObjectAccessType.DELETE);
        } else if (mappingPermissions.getMapFrom().startsWith(MatrixReferenceMappingPermissionConfig.CREATE_CHILD)) {
            String type = mappingPermissions.getMapFrom().split(":")[1];
            if (type.equals("*")) {
                //Получение всех имутабл полей
                List<String> immutableRefTypes = getImmutableRefTypes(typeName);
                for (String immutableRefType : immutableRefTypes) {
                    result.add(new CreateChildAccessType(immutableRefType));
                }
            } else {
                result.add(new CreateChildAccessType(type));
            }
        } else if (mappingPermissions.getMapFrom().startsWith(MatrixReferenceMappingPermissionConfig.EXECUTE)) {
            String action = mappingPermissions.getMapFrom().split(":")[1];
            if (action.equals("*")) {
                //Получение всех действий для типа
                List<String> actions = getActionsForType(typeName);
                for (String parentTypeAction : actions) {
                    result.add(new ExecuteActionAccessType(parentTypeAction));
                }
            } else {
                result.add(new ExecuteActionAccessType(action));
            }
        }
        return result;
    }

    private List<String> getActionsForType(String typeName) {
        List<String> result = new ArrayList<String>();
        Collection<ActionContextConfig> actionContexts = configurationExplorer.getConfigs(ActionContextConfig.class);
        if (actionContexts != null) {
            for (ActionContextConfig actionContext : actionContexts) {
                if (actionContext.getDomainObjectContext() != null) {
                    for (DomainObjectContextConfig domainObjectContextConfig : actionContext.getDomainObjectContext()) {
                        if (actionContext.getDomainObjectContext() != null &&
                                typeName.equals(domainObjectContextConfig.getDomainObjectType()) &&
                                actionContext.getAction() != null) {
                            for (ActionContextActionConfig actionContextActionConfig : actionContext.getAction()) {
                                result.add(actionContextActionConfig.getName());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isMappingToAccessType(MatrixReferenceMappingPermissionConfig mappingPermissions, AccessType accessType) {
        boolean result = false;
        if (accessType.equals(DomainObjectAccessType.READ)) {
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.READ);
        } else if (accessType.equals(DomainObjectAccessType.WRITE)) {
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.WRITE);
        } else if (accessType.equals(DomainObjectAccessType.DELETE)) {
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.DELETE);
        } else if (accessType instanceof CreateChildAccessType) {
            if (mappingPermissions.getMapTo().startsWith(MatrixReferenceMappingPermissionConfig.CREATE_CHILD)) {
                String type = mappingPermissions.getMapTo().split(":")[1];
                result = ((CreateChildAccessType) accessType).getChildType().equalsIgnoreCase(type) || type.equals("*");
            }
        } else if (accessType instanceof ExecuteActionAccessType) {
            if (mappingPermissions.getMapTo().startsWith(MatrixReferenceMappingPermissionConfig.EXECUTE)) {
                String action = mappingPermissions.getMapTo().split(":")[1];
                result = ((ExecuteActionAccessType) accessType).getActionName().equalsIgnoreCase(action) || action.equals("*");
            }
        }

        return result;
    }

    public List<String> getImmutableRefTypes(String domainObjectType) {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);

        List<String> immutableRefTypes = new ArrayList<String>();

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {

                if (((ReferenceFieldConfig) fieldConfig).isImmutable()) {
                    immutableRefTypes.add(((ReferenceFieldConfig) fieldConfig).getType());
                }
            }
        }
        return immutableRefTypes;
    }

    private String getQueryForCheckDomainObjectAccess(RdbmsId id) {
        String domainObjectTable = domainObjectTypeIdCache.getName(id.getTypeId());
        String domainObjectAclTable = getAclTableName(id);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTypeIdCache.getName(id.getTypeId())));
        //Флаг комбинированного заимствования прав, когда права на чтение заимствуются, а на запись и удаления настраиваются собственные
        final AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(domainObjectTable);
        boolean combinateAccessReference = AccessControlUtility.isCombineMatrixReference(accessMatrix);

        StringBuilder query = new StringBuilder();

        query.append("select count(*) from ").append(wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.");
        if (combinateAccessReference) {
            query.append(wrap("id"));
        } else {
            query.append(wrap("access_object_id"));
        }
        query.append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.")
                .append(wrap("id")).append(" = :object_id and a.")
                .append(wrap("operation")).append(" in (:operation)");
        return query.toString();
    }

    private String getQueryForCheckDomainObjectReadAccess(RdbmsId id) {
        RdbmsId checkId = id;
        String doType = domainObjectTypeIdCache.getName(id);

        //Если передали аудит лог то права берем из типа для кого этот аудит
        boolean isAuditLog = configurationExplorer.isAuditLogType(doType);
        String auditBaseTableName = null;
        if (isAuditLog) {
            doType = domainObjectQueryHelper.getRelevantType(doType);
            auditBaseTableName = DataStructureNamingHelper.getSqlName(
                    ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, doType));
            auditBaseTableName += "_al";
            //Подменяем id для получения корректной конфигурации
            checkId = getAuditedId(auditBaseTableName, id);
        }

        String domainObjectAclReadTable = getAclReadTableName(checkId);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, doType));

        StringBuilder query = new StringBuilder();

        query.append("select count(*) from ").append(wrap(domainObjectAclReadTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.").append(wrap("access_object_id"))
                .append(" = a.").append(wrap("object_id"));
        if (isAuditLog) {
            query.append(" inner join ").append(auditBaseTableName).append(" al on al.domain_object_id = o.id");
        }
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id");
        if (isAuditLog) {
            query.append(" and al.").append(wrap("id")).append(" = :object_id ");
        } else {
            query.append(" and o.").append(wrap("id")).append(" = :object_id ");
        }
        return query.toString();
    }

    @Nonnull
    private String getQueryForCheckAccessInAclTable(String aclTable, String objectBaseTable) {

        StringBuilder query = new StringBuilder();
        query.append("select count(*) from ").append(wrap(aclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
        query.append(" inner join ").append(wrap(objectBaseTable)).append(" o on o.");
        query.append(wrap("id"));
        query.append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.")
                .append(wrap("id")).append(" = :object_id and a.")
                .append(wrap("operation")).append(" in (:operation)");

        return query.toString();
    }

    /**
     * Получение аудируемого идентификатора
     * @param id
     * @return
     */
    private RdbmsId getAuditedId(String auditBaseTableName, final RdbmsId id) {
        String query = "select domain_object_id, domain_object_id_type from " + auditBaseTableName + " where id=:auditId";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("auditId", id.getId());

        RdbmsId result = jdbcTemplate.query(query, parameters, new ResultSetExtractor<RdbmsId>() {

            @Override
            public RdbmsId extractData(ResultSet rs) throws SQLException,
                    DataAccessException {
                if (rs.next()) {
                    return new RdbmsId(rs.getInt("domain_object_id_type"), rs.getInt("domain_object_id"));
                } else {
                    throw new ObjectNotFoundException(id);
                }
            }
        });
        return result;
    }

    @Override
    public Id[] checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type) {

        if (objectIds == null || objectIds.length == 0) {
            return new RdbmsId[0];
        }

        RdbmsId[] ids = Arrays.copyOf(objectIds, objectIds.length, RdbmsId[].class);

        String opCode = makeAccessTypeCode(type);

        List<Id> idsWithAllowedAccess = new ArrayList<Id>();
        IdSorterByType idSorterByType = new IdSorterByType(ids);

        Integer personIdType = domainObjectTypeIdCache.getId(GenericDomainObject.PERSON_DOMAIN_OBJECT);
        Id personId = new RdbmsId(personIdType, userId);

        // check configuration
        for (final Integer domainObjectTypeId : idSorterByType.getDomainObjectTypeIds()) {
            List<Id> idsOfOneType = idSorterByType.getIdsOfType(domainObjectTypeId);
            if (isAdministratorWithAlllPermissions(personId, domainObjectTypeId)) {
                idsWithAllowedAccess.addAll(idsOfOneType);
                continue;
            }

            /* Для проверки доступа на чтение вложений - собственный механизм */
            String domainObjectTypeName = domainObjectTypeIdCache.getName(domainObjectTypeId);
            if (DomainObjectAccessType.READ_ATTACH.equals(type)) {
                List<Id> idsWithAllowedAttachReadAccess = getIdsWithAllowedAttachReadAccess(userId, idsOfOneType, domainObjectTypeId);
                idsWithAllowedAccess.addAll(idsWithAllowedAttachReadAccess);
                continue;
            }

            // В случае непосредственных прав вызываем метод для всех идентификаторов, в случае с косвенными правами
            // получаем по каждому идентификатору результат отдельно
            String matrixRefType = configurationExplorer.getMatrixReferenceTypeName(domainObjectTypeName);
            if (matrixRefType == null) {
                List<Id> checkedIds = getIdsWithAllowedAccessByType(userId, opCode, idSorterByType, domainObjectTypeId);
                idsWithAllowedAccess.addAll(checkedIds);
            } else {
                for (Id id : idsOfOneType) {
                    if (checkDomainObjectAccess(userId, id, type)) {
                        idsWithAllowedAccess.add(id);
                    }
                }
            }
        }

        return idsWithAllowedAccess.toArray(new Id[idsWithAllowedAccess.size()]);
    }

    private boolean isAdministratorWithAlllPermissions(Id personId, Integer domainObjectTypeId) {
        if (domainObjectTypeId == null) {
            return false;
        }
        String domainObjectType = domainObjectTypeIdCache.getName(domainObjectTypeId);
        return userGroupCache.isAdministrator(personId)
                && (configurationExplorer.getAccessMatrixByObjectType(domainObjectType) == null);
    }

    /**
     * Возвращает список id доменных объектов одного типа, для которых разрешен
     * доступ.
     * @param userId
     *            id пользователя
     * @param opCode
     *            код операции доступа
     * @param sorterByType
     *            {@see IdSorterByType}
     * @param domainObjectType
     *            тип доменного объекта
     * @return
     */
    @Nonnull
    private List<Id> getIdsWithAllowedAccessByType(int userId, String opCode, IdSorterByType sorterByType, final Integer domainObjectType) {

        String query = getQueryForCheckMultiDomainObjectAccess(domainObjectTypeIdCache.getName(domainObjectType));
        return getIdsWithAllowedAccessByType(
                query, userId, opCode, sorterByType.getIdsOfType(domainObjectType), domainObjectType);
    }

    @Nonnull
    private List<Id> getIdsWithAllowedAttachReadAccess(int userId, @Nonnull List<Id> ids, @Nonnull final Integer domainObjectTypeId) {

        String domainObjectTypeName = domainObjectTypeIdCache.getName(domainObjectTypeId);
        boolean isAttach = configurationExplorer.isAttachmentType(domainObjectTypeName);
        if (!isAttach) {
            /* для не-вложений такой тип доступа не используется */
            return Collections.emptyList();
        }

        List<Id> idsWithAllowedAccess = new LinkedList<>();

        Set<Id> sCheckedIds = permissionServiceDao.checkIfContentReadAllowedForAll(ids, domainObjectTypeName);
        idsWithAllowedAccess.addAll(sCheckedIds);

        Set<Id> nonCheckedIds = new HashSet<>(ids);
        nonCheckedIds.removeAll(sCheckedIds);

        String query = getQueryForCheckMultiAttachReadAccess(domainObjectTypeName);
        List<Id> lCheckedIds = getIdsWithAllowedAccessByType(
                query, userId, AccessControlConverter.OP_READ_ATTACH, new ArrayList<>(nonCheckedIds), domainObjectTypeId);
        idsWithAllowedAccess.addAll(lCheckedIds);

        return idsWithAllowedAccess;
    }

    @Nonnull
    private List<Id> getIdsWithAllowedAccessByType(@Nonnull String query, int userId, @Nonnull String opCode, @Nonnull List<Id> ids,
            @Nonnull final Integer domainObjectType) {

        List<Long> listIds = AccessControlUtility.convertRdbmsIdsToLongIds(new ArrayList<>(ids));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
        parameters.put("object_ids", listIds);
        parameters.put("operation", opCode);

        RowMapper<Id> extractor = (rs, rowNum) -> {
            long objectId = rs.getLong("object_id");
            return new RdbmsId(domainObjectType, objectId);
        };

        return jdbcTemplate.query(query, parameters, extractor);
    }

    private String getQueryForCheckMultiDomainObjectAccess(String domainObjectType) {
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectType));
        String domainObjectAclTable = AccessControlUtility.getAclTableNameFor(domainObjectType);

        StringBuilder query = new StringBuilder();

        query.append("select a.").append(wrap("object_id")).append(" object_id from ")
                .append(wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.").append(wrap("child_group_id"))
                .append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.").append(wrap("access_object_id"))
                .append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.").append(wrap("id"))
                .append(" in (:object_ids) and ")
                .append("a.").append(wrap("operation")).append(" = :operation");
        return query.toString();
    }

    @Nonnull
    private String getQueryForCheckMultiAttachReadAccess(@Nonnull String domainObjectType) {

        String domainObjectAclTable = AccessControlUtility.getAclTableNameFor(domainObjectType);

        StringBuilder query = new StringBuilder();

        query.append("select a.").append(wrap("object_id")).append(" object_id from ")
                .append(wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.").append(wrap("child_group_id"))
                .append(" = gm.").append(wrap("usergroup"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and a.").append(wrap("object_id"))
                .append(" in (:object_ids)")
                .append(" and a.").append(wrap("operation")).append(" = :operation");

        return query.toString();
    }

    @Override
    public AccessType[] checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types) {
        RdbmsId id = (RdbmsId) objectId;
        String[] opCodes = makeAccessTypeCodes(types);
        List<String> lOpCodes = Arrays.asList(opCodes);

        String query = getQueryForCheckDomainObjectMultiAccess(id);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_id", id.getId());
        parameters.put("operations", lOpCodes);

        List<AccessType> res = jdbcTemplate.query(query, parameters, new RowMapper<AccessType>() {
            private long start = System.currentTimeMillis();

            @Override
            public AccessType mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResultSetExtractionLogger.log("PostgresDatabaseAccessAgent.checkDomainObjectMultiAccess", start, rowNum);
                String code = rs.getString("operation");
                return decodeAccessType(code);
            }
        });

        /* Для прав на чтение вложений обработка не такая, как для остальных */
        String objTypeName;
        if (lOpCodes.contains(AccessControlConverter.OP_READ_ATTACH) && !res.contains(DomainObjectAccessType.READ_ATTACH)
                && configurationExplorer.isAttachmentType(objTypeName = domainObjectTypeIdCache.getName(id))) {
            AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectType(objTypeName);
            boolean aclInheritanceInUse = accessMatrix == null || accessMatrix.getMatrixReference() != null;
            if (aclInheritanceInUse && checkAttachReadContentAccess(userId, id)) {
                res = new ArrayList<>(res);
                res.add(DomainObjectAccessType.READ_ATTACH);
            }
        }

        return res.toArray(new AccessType[0]);
    }

    private String getQueryForCheckDomainObjectMultiAccess(RdbmsId id) {
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTypeIdCache.getName(id.getTypeId())));
        String domainObjectAclTable = getAclTableName(id);

        StringBuilder query = new StringBuilder();

        query.append("select a.").append(wrap("operation")).append(" operation from ")
                .append(wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.").append(wrap("child_group_id"))
                .append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.").append(wrap("access_object_id"))
                .append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.").append(wrap("id"))
                .append(" = :object_id and a.")
                .append(wrap("operation")).append(" in (:operations)");

        return query.toString();
    }

    private String getAclTableName(RdbmsId id) {
        String domainObjectTable = domainObjectTypeIdCache.getName(id.getTypeId());

        //Флаг комбинированного заимствования прав, когда права на чтение заимствуются, а на запись и удаления настраиваются собственные
        final AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(domainObjectTable);
        boolean combinateAccessReference = AccessControlUtility.isCombineMatrixReference(accessMatrix);

        if (!combinateAccessReference) {
            domainObjectTable = getDomainObjectTypeWithInheritedAccess(id, domainObjectTable);
        }

        return AccessControlUtility.getAclTableNameFor(domainObjectTable);
    }

    private String getDomainObjectTypeWithInheritedAccess(RdbmsId id, String sourceObjectType) {
        String domainObjectTable = sourceObjectType;
        // Проверяем нет ли заимствования прав и в случае наличия подменяем тип откуда берем права
        String martixRef = configurationExplorer.getMatrixReferenceTypeName(sourceObjectType);
        if (martixRef != null) {
            // Получаем реальный тип объекта у которого заимствуются права, для этого делаем запрос к martixRef и пол
            domainObjectTable = getMatrixRefType(sourceObjectType, martixRef, id);
        }
        return domainObjectTable;
    }

    private String getAclReadTableName(RdbmsId id) {
        String domainObjectTable = domainObjectTypeIdCache.getName(id.getTypeId());
        domainObjectTable = getDomainObjectTypeWithInheritedAccess(id, domainObjectTable);
        return AccessControlUtility.getAclReadTableNameFor(configurationExplorer, domainObjectTable);
    }

    /**
     * Получение имени типа у которого заимствуются права. При этом учитывается
     * то что в матрице при заимствование может быть указан атрибут ссылающийся
     * на родительский тип того объекта, у которого реально надо взять матрицу
     * прав
     * @param childType
     * @param parentType
     * @param id
     * @return
     */
    private String getMatrixRefType(String childType, String parentType, final RdbmsId id) {
        String rootForChildType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, childType);
        String rootForParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, parentType);
        String query =
                "select p.id_type from " + rootForChildType + " c inner join " + rootForParentType + " p on (c.access_object_id = p.id) where c.id = :id";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id.getId());

        int typeId = jdbcTemplate.query(query, parameters, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException,
                    DataAccessException {
                if (!rs.next()) {
                    throw new ObjectNotFoundException(id);
                }
                return rs.getInt("id_type");
            }
        });

        return domainObjectTypeIdCache.getName(typeId);
    }

    public String makeAccessTypeCode(AccessType type) {

        if (type == null || type.equals(DomainObjectAccessType.READ)) {
            throw new IllegalArgumentException("Unsupported access type: " + type);
        }

        String code = accessControlConverter.accessTypeToCode(type);
        if (code == null) {
            throw new IllegalArgumentException("Unknown access type: " + type);
        }

        return code;
    }

    private String[] makeAccessTypeCodes(AccessType[] types) {
        String[] codes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = makeAccessTypeCode(types[i]);
        }
        return codes;
    }

    private AccessType decodeAccessType(String code) {
        AccessType accessType = accessControlConverter.codeToAccessType(code);
        if (accessType == null) {
            throw new IllegalArgumentException("Unknown access type: " + code);
        }
        return accessType;
    }

    @Override
    public boolean isAllowedToCreateByStaticGroups(Id userId, String objectType) {
        AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(objectType);
        logger.trace("Access matrix to {}: {}", objectType, accessMatrix);

        String checkType = null;
        if (accessMatrix != null && (accessMatrix.getBorrowPermissisons() == null || accessMatrix.getBorrowPermissisons() == BorrowPermissisonsMode.all)) {
            checkType = configurationExplorer.getMatrixReferenceTypeName(objectType);
            if (checkType == null) {
                checkType = objectType;
            }
        } else {
            checkType = objectType;
        }
        logger.trace("Check type is {}", checkType);

        return isAllowedToCreateObjectType(userId, checkType);
    }

    @Override
    public boolean isAllowedToCreateByStaticGroups(Id userId, DomainObject domainObject) {
        AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(domainObject.getTypeName());
        logger.trace("Access matrix to {}: {}", domainObject.getTypeName(), accessMatrix);

        String checkType = null;
        if (accessMatrix != null && (accessMatrix.getBorrowPermissisons() == null || accessMatrix.getBorrowPermissisons() == BorrowPermissisonsMode.all)) {
            String objectType = domainObject.getTypeName();
            checkType = getMatrixReferenceActualFieldType(domainObject);
            if (checkType == null) {
                checkType = objectType;
            }
        } else {
            checkType = domainObject.getTypeName();
        }
        logger.trace("Check type is {}", checkType);

        return isAllowedToCreateObjectType(userId, checkType);
    }

    @Override
    public String getStatus(Id objectId) {
        String typeName = domainObjectTypeIdCache.getName(objectId);
        String rootType = configurationExplorer.getDomainObjectRootType(typeName);

        String query = "select s.name from status s join " + rootType + " t on t.status = s.id where t.id = :id";
        List<String> queryResult = jdbcTemplate.queryForList(query,
                Collections.singletonMap("id", ((RdbmsId)objectId).getId()),
                String.class);
        if (queryResult.size() > 0){
            return queryResult.get(0);
        }else{
            return null;
        }
    }

    /**
     * Получение имени типа доменного объекта, который необходимо использовать
     * при вычислении прав на доменный объект в случае использования
     * заимствования прав у связанного объекта. Важно: метод возвращает ссылку
     * на непосредственно объект конфигурации. Изменение данного объекта
     * недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObject
     * @return
     */
    public String getMatrixReferenceActualFieldType(DomainObject domainObject) {
        DomainObjectTypeConfig childDomainObjectTypeConfig = childDomainObjectTypeMap.get(domainObject.getTypeName());
        logger.trace("Child Domain Object Type Config from map {}", childDomainObjectTypeConfig);        
        if (childDomainObjectTypeConfig == null) {
            childDomainObjectTypeConfig = findChildDomainObjectTypeConfig(domainObject.getTypeName());
            logger.trace("Child Domain Object Type Config from find {}", childDomainObjectTypeConfig);        
        }

        if (NullValues.isNull(childDomainObjectTypeConfig)) {
            return null;
        }

        AccessMatrixConfig matrixConfig = accessMatrixConfigMap.get(domainObject.getTypeName());
        logger.trace("Access matrix config {}", matrixConfig);        

        String result = null;

        if (matrixConfig != null && matrixConfig.getMatrixReference() != null) {
            // Получаем имя типа на которого ссылается martix-reference-field
            String matrixReferenceField = matrixConfig.getMatrixReference();
            Id parentId = domainObject.getReference(matrixReferenceField);
            logger.trace("Parent id is {}", parentId);
            if (parentId == null) {
                throw new RuntimeException("Matrix referenece field: " + matrixReferenceField +
                        " is not a reference field in " + childDomainObjectTypeConfig.getName());
            }

            // Вызываем рекурсивно метод для родительского типа, на случай если в родительской матрице так же заполнено
            // поле martix-reference-field

            String parentTypeName = domainObjectTypeIdCache.getName(parentId);
            logger.trace("Parent type is {}", parentTypeName);
            
            AccessMatrixConfig parentMatrixConfig = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(parentTypeName);
            logger.trace("Parent matrix config is {}", parentMatrixConfig);

            if (parentMatrixConfig != null && parentMatrixConfig.getMatrixReference() != null) {
                AccessToken systemAccessToken = accessControlService.createSystemAccessToken(getClass().getName());
                DomainObject parentDomainObject = domainObjectDao.find(parentId, systemAccessToken);
                result = getMatrixReferenceActualFieldType(parentDomainObject);
            }

            // В случае если у родителя не заполнен атрибут martix-reference-field то возвращаем имя родителя
            if (result == null) {
                result = parentTypeName;
            }

        }
        
        logger.trace("Get Matrix Reference ActualField Type return {}", result);        
        return result;
    }

    /**
     * Получение конфигурации доменного объекта где определена матрица с учетом иерархии.
     * В методе инициализируется кэш конфигураций, поэтому он синхронный
     * @param domainObjectTypeName
     * @return
     */
    private synchronized DomainObjectTypeConfig findChildDomainObjectTypeConfig(String domainObjectTypeName) {
        String childTypeName = domainObjectTypeName;
        // Получаем матрицу и смотрим атрибут matrix_reference_field
        DomainObjectTypeConfig childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childTypeName);

        AccessMatrixConfig matrixConfig = null;

        // Ищим матрицу для типа с учетом иерархии типов
        while ((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                && childDomainObjectTypeConfig.getExtendsAttribute() != null) {
            childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
        }

        childDomainObjectTypeMap.put(domainObjectTypeName, childDomainObjectTypeConfig);
        accessMatrixConfigMap.put(domainObjectTypeName, matrixConfig);

        return childDomainObjectTypeConfig;
    }

    private boolean isAllowedToCreateObjectType(Id userId, String checkType) {
        List<String> userGroups = configurationExplorer.getAllowedToCreateUserGroups(checkType);
        logger.trace("Allowed To Create User Groups {}", userGroups);

        if (userGroups.size() == 0) {
            return false;
        }

        if (userId == null) {
            return false;
        }

        if (userGroups.contains(ALL_PERSONS_GROUP)) {
            return true;
        }

        Filter filter = new Filter();
        filter.setFilter("byGroupsAndPerson");
        filter.addMultiStringCriterion(0, userGroups);
        filter.addReferenceCriterion(1, userId);

        AccessToken systemAccessToken = accessControlService.createSystemAccessToken(getClass().getName());
        IdentifiableObjectCollection persons = collectionsDao.findCollection("IsPersonInGroups",
                Collections.singletonList(filter), null, 0, 1, systemAccessToken);
        logger.trace("PersonInGroups collection size: {}", persons != null ? persons.size() : null);
        
        return persons != null && persons.size() > 0;
    }

}
