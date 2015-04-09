package ru.intertrust.cm.core.dao.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.MatrixReferenceMappingConfig;
import ru.intertrust.cm.core.config.MatrixReferenceMappingPermissionConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionContextConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;
import ru.intertrust.cm.core.dao.impl.utils.ConfigurationExplorerUtils;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.IdSorterByType;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация агента БД по запросам прав доступа для PostgreSQL.
 * <p>Перед использованием объекта необходимо установить корректный источник данных вызовом метода
 * {@link #setDataSource(DataSource)}. Обычно это делается через соотвествующий атрибут или тег конфигурации
 * объекта в Spring-контексте приложения (beans.xml), где создаётся и сам объект.
 *
 * @author apirozhkov
 */
public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {

    private static final String ALL_PERSONS_GROUP = "*";

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired    
    private ConfigurationExplorer configurationExplorer;
    
    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;
 
    @Autowired
    private UserGroupGlobalCache userGroupCache;
    
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setDomainObjetcTypeIdCache(DomainObjectTypeIdCache domainObjetcTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjetcTypeIdCache;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }        
    
    /**
     * Устанавливает источник данных, который будет использоваться для выполнения запросов.
     *
     * @param dataSource Источник данных
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type) {
        RdbmsId id = (RdbmsId) objectId;
        
        //В случае наличия заимствования прав меняется проверяемый тип доступа
        List<AccessType> checkAccessType = getMatrixReferencePermission(domainObjectTypeIdCache.getName(id.getTypeId()), type);
        
        List<String> opCode = new ArrayList<String>();
        boolean opRead = false;
        for (AccessType accessType : checkAccessType) {
            //Особым образом обрабатываем READ, так как права на чтение хранятся в другой таблице
            if (accessType.equals(DomainObjectAccessType.READ)){
                opRead = true;
            }else{
                opCode.add(makeAccessTypeCode(accessType));
            }
        }
        
        boolean result = false;
        if (opRead){
            result = checkDomainObjectReadAccess(userId, objectId); 
        }else{
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
     * Метод получает права, которые необходимо проверить с учетом мапинга прав в случае заимствования прав
     * @param type права которые проверяются
     * @return права которые необходимо проверить на данных у типа, права которого заимствуются
     */
    private List<AccessType> getMatrixReferencePermission(String typeName, AccessType accessType){
        List<AccessType> result = new ArrayList<AccessType>();
        //Проверяем нет ли заимствования прав и в случае наличия подменяем тип доступа согласно мапингу
        String martixRef = configurationExplorer.getMatrixReferenceTypeName(typeName);
        if (martixRef != null){
            //Получаем маппинг прав
            AccessMatrixConfig martix = configurationExplorer.getAccessMatrixByObjectType(typeName);
            if (martix.getMatrixReferenceMappingConfig() != null){
                //Берем мапинг из матрицы
                MatrixReferenceMappingConfig mapping = martix.getMatrixReferenceMappingConfig();
                if (mapping != null){
                    for (MatrixReferenceMappingPermissionConfig mappingPermissions : mapping.getPermission()) {
                        if (isMappingToAccessType(mappingPermissions, accessType)){
                            List<AccessType> mappedAccessType = getAccessTypeFromMapping(martixRef, mappingPermissions);
                            result.addAll(mappedAccessType);
                        }                    
                    }
                }
            }else{
                //Маппинг по умолчанию read->read, write->write+delete, delete->delete
                result.add(accessType);
                if (accessType.equals(DomainObjectAccessType.DELETE)){
                    result.add(DomainObjectAccessType.WRITE);
                }
            }
        }else{
            result.add(accessType);
        }
        return result;
    }
    
    
    private List<AccessType> getAccessTypeFromMapping(String typeName, MatrixReferenceMappingPermissionConfig mappingPermissions) {
        List<AccessType> result = new ArrayList<AccessType>();;
        if (mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.READ)){
            result.add(DomainObjectAccessType.READ);
        }else if(mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.WRITE)){
            result.add(DomainObjectAccessType.WRITE);
        }else if(mappingPermissions.getMapFrom().equals(MatrixReferenceMappingPermissionConfig.DELETE)){
            result.add(DomainObjectAccessType.DELETE);
        }else if(mappingPermissions.getMapFrom().startsWith(MatrixReferenceMappingPermissionConfig.CREATE_CHILD)){
            String type = mappingPermissions.getMapFrom().split(":")[1];
            if (type.equals("*")){
                //Получение всех имутабл полей
                List<String> immutableRefTypes = getImmutableRefTypes(typeName);
                for (String immutableRefType : immutableRefTypes) {
                    result.add(new CreateChildAccessType(immutableRefType));
                }
            }else{
                result.add(new CreateChildAccessType(type));
            }
        }else if(mappingPermissions.getMapFrom().startsWith(MatrixReferenceMappingPermissionConfig.EXECUTE)){
            String action = mappingPermissions.getMapFrom().split(":")[1];
            if (action.equals("*")){
                //Получение всех действий для типа
                List<String> actions = getActionsForType(typeName);
                for (String parentTypeAction : actions) {
                    result.add(new ExecuteActionAccessType(parentTypeAction));
                }
            }else{
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
        if (accessType.equals(DomainObjectAccessType.READ)){
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.READ);
        }else if(accessType.equals(DomainObjectAccessType.WRITE)){
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.WRITE);
        }else if(accessType.equals(DomainObjectAccessType.DELETE)){
            result = mappingPermissions.getMapTo().equals(MatrixReferenceMappingPermissionConfig.DELETE);
        }else if(accessType instanceof CreateChildAccessType){
            if (mappingPermissions.getMapTo().startsWith(MatrixReferenceMappingPermissionConfig.CREATE_CHILD)){
                String type = mappingPermissions.getMapTo().split(":")[1];
                result = ((CreateChildAccessType)accessType).getChildType().equalsIgnoreCase(type) || type.equals("*");
            }
        }else if(accessType instanceof ExecuteActionAccessType){
            if (mappingPermissions.getMapTo().startsWith(MatrixReferenceMappingPermissionConfig.EXECUTE)){
                String action = mappingPermissions.getMapTo().split(":")[1];
                result = ((ExecuteActionAccessType)accessType).getActionName().equalsIgnoreCase(action) || action.equals("*");
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
        String domainObjectAclTable = getAclTableName(id);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTypeIdCache.getName(id.getTypeId()))); 

        
        StringBuilder query = new StringBuilder();
        
        query.append("select count(*) from ").append(wrap(domainObjectAclTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.").append(wrap("access_object_id"))
                .append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.")
                .append(wrap("id")).append(" = :object_id and a.")
                .append(wrap("operation")).append(" in (:operation)");
        return query.toString();
    }

    private String getQueryForCheckDomainObjectReadAccess(RdbmsId id) {
        String domainObjectAclReadTable = getAclReadTableName(id);
        String domainObjectBaseTable = DataStructureNamingHelper.getSqlName(
                ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, domainObjectTypeIdCache.getName(id.getTypeId()))); 
        
        StringBuilder query = new StringBuilder();
        
        query.append("select count(*) from ").append(wrap(domainObjectAclReadTable)).append(" a ");
        query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
        //Добавляем этот фрагмент в связи с добавлением правил заимствования прав
        query.append(" inner join ").append(wrap(domainObjectBaseTable)).append(" o on o.").append(wrap("access_object_id"))
                .append(" = a.").append(wrap("object_id"));
        query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and o.")
                .append(wrap("id")).append(" = :object_id ");
        return query.toString();
    }

    @Override
    public Id[] checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type) {

        RdbmsId[] ids = Arrays.copyOf(objectIds, objectIds.length, RdbmsId[].class);

        String opCode = makeAccessTypeCode(type);
        if (objectIds == null || objectIds.length == 0) {
            return new RdbmsId[0];
        }

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

            // В случае непосредственных прав вызываем метод для всех идентификаторов, в случае с косвенными правами
            // получаем по каждому идентификатору результат отдельно
            String matrixRefType = configurationExplorer.getMatrixReferenceTypeName(domainObjectTypeIdCache.getName(domainObjectTypeId));
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
     * Возвращает список id доменных объектов одного типа, для которых разрешен доступ.
     * @param userId id пользователя
     * @param opCode код операции доступа
     * @param sorterByType {@see IdSorterByType}
     * @param domainObjectType тип доменного объекта
     * @return
     */
    private List<Id> getIdsWithAllowedAccessByType(int userId, String opCode, IdSorterByType sorterByType,
            final Integer domainObjectType) {
        String query = getQueryForCheckMultiDomainObjectAccess(domainObjectTypeIdCache.getName(domainObjectType));

        List<Long> listIds = AccessControlUtility.convertRdbmsIdsToLongIds(sorterByType.getIdsOfType(domainObjectType));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_ids", listIds);
        parameters.put("operation", opCode);

        List<Id> checkedIds = jdbcTemplate.query(query, parameters, new RowMapper<Id>() {

            @Override
            public Id mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long objectId = rs.getLong("object_id");
                RdbmsId id = new RdbmsId(domainObjectType, objectId);
                return id;
            }

        });
        return checkedIds;
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

    @Override
    public AccessType[] checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types) {
        RdbmsId id = (RdbmsId) objectId;
        String[] opCodes = makeAccessTypeCodes(types);

        String query = getQueryForCheckDomainObjectMultiAccess(id);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("object_id", id.getId());
        parameters.put("operations", Arrays.asList(opCodes));

        return jdbcTemplate.query(query, parameters, new RowMapper<AccessType>() {
            @Override
            public AccessType mapRow(ResultSet rs, int rowNum) throws SQLException {
                String code = rs.getString("operation");
                return decodeAccessType(code);
            }
        }).toArray(new AccessType[0]);
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
        
        domainObjectTable = getDomainObjectTypeWithInheritedAccess(id, domainObjectTable);
        
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
     * Получение имени типа у которого заимствуются права. При этом учитывается то что в матрице при заимствование 
     * может быть указан атрибут ссылающийся на родительский тип того объекта, у которого реально надо взять матрицу прав
     * @param childType
     * @param parentType
     * @param id
     * @return
     */
    private String getMatrixRefType(String childType, String parentType, final RdbmsId id){
    	String rootForChildType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, childType);
    	String rootForParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, parentType);
    	String query = "select p.id_type from " + rootForChildType + " c inner join " + rootForParentType + " p on (c.access_object_id = p.id) where c.id = :id";
    	
    	Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id.getId());

        int typeId = jdbcTemplate.query(query, parameters, new ResultSetExtractor<Integer>(){

			@Override
			public Integer extractData(ResultSet rs) throws SQLException,
					DataAccessException {
                if (!rs.next()) {
                    throw new ObjectNotFoundException(id);
                }
				return rs.getInt("id_type");
			}});
        
        return domainObjectTypeIdCache.getName(typeId);
    }
    
    public static String makeAccessTypeCode(AccessType type) {
        // Разрешения на чтение хранятся в отдельной таблице, поэтому код "R" не используется
        /*if (DomainObjectAccessType.READ.equals(type)) {
            return "R";
        }*/
        if (DomainObjectAccessType.WRITE.equals(type)) {
            return "W";
        }
        if (DomainObjectAccessType.DELETE.equals(type)) {
            return "D";
        }
        if (CreateChildAccessType.class.equals(type.getClass())) {
            CreateChildAccessType ccType = (CreateChildAccessType) type;
            return new StringBuilder("C_").append(ccType.getChildType()).toString();
        }
        if (ExecuteActionAccessType.class.equals(type.getClass())) {
            ExecuteActionAccessType executeActionType = (ExecuteActionAccessType) type;
            return new StringBuilder("E_").append(executeActionType.getActionName()).toString();
        }

        throw new IllegalArgumentException("Unknown access type: " + type);
    }

    private static String[] makeAccessTypeCodes(AccessType[] types) {
        String[] codes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = makeAccessTypeCode(types[i]);
        }
        return codes;
    }

    private static AccessType decodeAccessType(String code) {
        String[] codeParts = code.split("_", 2);
        if ("W".equals(codeParts[0])) {
            return DomainObjectAccessType.WRITE;
        }
        if ("D".equals(codeParts[0])) {
            return DomainObjectAccessType.DELETE;
        }
        if ("C".equals(codeParts[0])) {
            return new CreateChildAccessType(codeParts[1]);
        }
        throw new IllegalArgumentException("Unknown access type: " + code);
    }

    @Override
    public boolean checkUserGroup(int userId, String groupName) {
        String query = getQueryForCheckUserGroup();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("user_id", userId);
        parameters.put("group_name", groupName);
        Integer result = jdbcTemplate.queryForObject(query, parameters, Integer.class);
        return result > 0;
    }

    private String getQueryForCheckUserGroup() {
        StringBuilder query = new StringBuilder();
        
        query.append("select count(*) from ").append(wrap("user_group")).append(" ug ");
        query.append("inner join ").append(wrap("group_group")).append(" gg on ug.").append(wrap("id"))
                .append(" = gg.").append(wrap("parent_group_id"));
        query.append("inner join ").append(wrap("group_member")).append(" gm on gg.").append(wrap("child_group_id"))
                .append(" = gm.").append(wrap("usergroup"));
        query.append("where gm.").append(wrap("person_id")).append(" = :user_id and ug.").append(wrap("group_name"))
                .append(" = :group_name");
        return query.toString();
    }

    @Override
    public boolean isAllowedToCreateByStaticGroups(Id userId, String objectType) {
        String checkType = configurationExplorer.getMatrixReferenceTypeName(objectType);
        if (checkType == null) {
            checkType = objectType;
        }

        return isAllowedToCreateObjectType(userId, checkType);
    }
    
    @Override
    public boolean isAllowedToCreateByStaticGroups(Id userId, DomainObject domainObject) {
        String objectType = domainObject.getTypeName();
        String checkType = getMatrixReferenceActualFieldType(domainObject);
        if (checkType == null) {
            checkType = objectType;
        }

        return isAllowedToCreateObjectType(userId, checkType);
    }

    /**
     * Получение имени типа доменного объекта, который необходимо использовать при вычислении прав на доменный объект в
     * случае использования заимствования прав у связанного объекта. Важно: метод возвращает ссылку на непосредственно
     * объект конфигурации. Изменение данного объекта недопустимо и напрямую приводит к некорректной работе приложения
     * @param domainObject
     * @return
     */
    public String getMatrixReferenceActualFieldType(DomainObject domainObject) {
        String childTypeName = domainObject.getTypeName();
        // Получаем матрицу и смотрим атрибут matrix_reference_field
        AccessMatrixConfig matrixConfig = null;
        DomainObjectTypeConfig childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childTypeName);
        if (childDomainObjectTypeConfig == null) {
            return null;
        }

        String result = null;

        // Ищим матрицу для типа с учетом иерархии типов
        while ((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                && childDomainObjectTypeConfig.getExtendsAttribute() != null) {
            childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
        }

        if (matrixConfig != null && matrixConfig.getMatrixReference() != null) {
            // Получаем имя типа на которого ссылается martix-reference-field
            String matrixReferenceField = matrixConfig.getMatrixReference();
            Id parentId = domainObject.getReference(matrixReferenceField);
            if (parentId == null) {
                throw new RuntimeException("Matrix referenece field: " + matrixReferenceField + " is not a reference field in " + childTypeName);
            }

            // Вызываем рекурсивно метод для родительского типа, на случай если в родительской матрице так же заполнено
            // поле martix-reference-field

            String parentTypeName = domainObjectTypeIdCache.getName(parentId);
            AccessMatrixConfig parentMatrixConfig = configurationExplorer.getAccessMatrixByObjectType(parentTypeName);
            
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
        return result;
    }
    
    private boolean isAllowedToCreateObjectType(Id userId, String checkType) {
        List<String> userGroups = configurationExplorer.getAllowedToCreateUserGroups(checkType);

        if (userGroups.size() == 0) {
            return false;
        }

        if (userId == null) {
            return false;
        }

        if (userGroups.contains(ALL_PERSONS_GROUP)) {
            return true;
        }

        String query = "select gm." + wrap("person_id") + ", gm." + wrap("person_id_type") + " " +
                "from " + wrap("group_member") + " gm " +
                "inner join " + wrap("group_group") + " gg on gg." + wrap("child_group_id") + " = gm." + wrap("usergroup") + " " +
                "inner join " + wrap("user_group") + " ug on gg." + wrap("parent_group_id") + "= ug." + wrap("id") + " " +
                "where ug." + wrap("group_name") + " in (:groups)";

        Map<String, Object> params = new HashMap<>();
        params.put("groups", userGroups);

        List<Id> groupMembers = jdbcTemplate.query(query, params, new RowMapper<Id>() {

            @Override
            public Id mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long personId = rs.getLong("person_id");
                int personObjectType = rs.getInt("person_id_type");
                RdbmsId id = new RdbmsId(personObjectType, personId);
                return id;
            }

        });

        if (groupMembers.contains(userId)) {
            return true;
        }
        return false;
    }

}
