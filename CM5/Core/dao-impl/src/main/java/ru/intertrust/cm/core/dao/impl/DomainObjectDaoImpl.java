package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.utils.*;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author atsvetkov
 */

public class DomainObjectDaoImpl implements DomainObjectDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdGenerator idGenerator;

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Устанавливает генератор для создания уникальных идентифиткаторово
     *
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     *
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public DomainObject create(DomainObject domainObject) {
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(domainObject.getTypeName());

        validateParentIdType(domainObject, domainObjectTypeConfig);

        String query = generateCreateQuery(domainObjectTypeConfig);

        Object nextId = idGenerator.generatetId(domainObjectTypeConfig);

        RdbmsId id = new RdbmsId(domainObject.getTypeName(), (Long) nextId);

        domainObject.setId(id);

        Map<String, Object> parameters = initializeCreateParameters(domainObject, domainObjectTypeConfig);

        jdbcTemplate.update(query, parameters);

        return domainObject;
    }

    @Override
    public DomainObject save(DomainObject domainObject) throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        if (domainObject.isNew()) {
            return create(domainObject);
        }

        return update(domainObject);
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        List<DomainObject> result = new ArrayList();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject;
            try {
                newDomainObject = save(domainObject);
                result.add(newDomainObject);
            } catch (Exception e) {
                // TODO: пока ничего не делаем...разобраться как обрабатывать ошибки
            }

        }

        return result;

    }

    @Override
    public DomainObject update(DomainObject domainObject) throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(domainObject.getTypeName());

        validateIdType(domainObject.getId());
        validateParentIdType(domainObject, domainObjectTypeConfig);

        String query = generateUpdateQuery(domainObjectTypeConfig);

        Date currentDate = new Date();

        Map<String, Object> parameters = initializeUpdateParameters(domainObject, domainObjectTypeConfig, currentDate);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(domainObject.getId()))) {
            throw new ObjectNotFoundException(domainObject.getId());
        }

        if (count == 0) {
            throw new OptimisticLockException(domainObject);
        }

        domainObject.setModifiedDate(currentDate);

        return domainObject;

    }

    @Override
    public void delete(Id id) throws InvalidIdException, ObjectNotFoundException {
        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(rdbmsId.getTypeName());
        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0) {
            throw new ObjectNotFoundException(rdbmsId);
        }

    }

    @Override
    public int delete(Collection<Id> ids) {
        // TODO как обрабатывать ошибки при удалении каждого доменного объекта...
        int count = 0;
        for (Id id : ids) {
            try {
                delete(id);

                count++;
            } catch (ObjectNotFoundException e) {
                //ничего не делаем пока
            } catch (InvalidIdException e) {
                ////ничего не делаем пока
            }

        }
        return count;
    }

    @Override
    public boolean exists(Id id) throws InvalidIdException {
        RdbmsId rdbmsId = (RdbmsId) id;

        String query = generateExistsQuery(rdbmsId.getTypeName());

        validateIdType(id);

        Map<String, Long> parameters = initializeExistsParameters(id);

        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;

    }

    @Override
    public DomainObject find(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;

        String tableName = DataStructureNamingHelper.getSqlName(rdbmsId.getTypeName());

        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID=:id ");
        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        return jdbcTemplate.query(query.toString(), parameters, new SingleObjectRowMapper(rdbmsId.getTypeName()));
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        String tableName = DataStructureNamingHelper.getSqlName(getDomainObjectType(ids));

        String idsList = convertIdsToCommaSeparatedString(ids);
        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID in ( " + idsList + " ) ");

        return jdbcTemplate.query(query.toString(), new MultipleObjectRowMapper(tableName));
    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject           доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(DomainObject domainObject,
                                                             DomainObjectTypeConfig domainObjectTypeConfig) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        parameters.put("parent", getParentId(domainObject));
        parameters.put("created_date", domainObject.getCreatedDate());
        parameters.put("updated_date", domainObject.getModifiedDate());

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Создает SQL запрос для модификации доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для модиификации доменного объекта с параметрами
     */
    protected String generateUpdateQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        List<String> columnNames = DataStructureNamingHelper.getSqlName(feldConfigs);

        String fieldsWithparams = DaoUtils.generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(tableName).append(" set ");
        query.append("UPDATED_DATE=:current_date, ").append(PARENT_COLUMN).append("=:parent, ");
        query.append(fieldsWithparams);
        query.append(" where ID=:id");
        query.append(" and UPDATED_DATE=:updated_date");

        return query.toString();

    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject           доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeUpdateParameters(DomainObject domainObject,
                                                             DomainObjectTypeConfig domainObjectTypeConfig, Date currentDate) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", currentDate);
        parameters.put("updated_date", domainObject.getModifiedDate());
        parameters.put("parent", getParentId(domainObject));

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;

    }

    /**
     * Создает SQL запрос для создания доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для создания доменного объекта с параметрами
     */
    protected String generateCreateQuery(DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getFieldConfigs();

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper.getSqlName(fieldConfigs);

        String commaSeparatedColumns = StringUtils.collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils.generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append(" (");
        query.append("ID, ").append(PARENT_COLUMN).append(", CREATED_DATE, UPDATED_DATE, " +
                "").append(commaSeparatedColumns);
        query.append(") values (");
        query.append(":id , :parent, :created_date, :updated_date, ");
        query.append(commaSeparatedParameters);
        query.append(")");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateDeleteQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления всех доменных объектов
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для удаления всех доменных объектов
     */
    protected String generateDeleteAllQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления доменного объекта
     *
     * @param id идентификатор доменного объекта для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeIdParameter(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        return parameters;
    }

    /**
     * Создает SQL запрос для проверки существует ли доменный объект
     *
     * @param domainObjectName название доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateExistsQuery(String domainObjectName) {

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectName);

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления доменного объекта
     *
     * @param id идентификатор доменных объектов для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Long> initializeExistsParameters(Id id) {

        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Long> parameters = new HashMap<String, Long>();
        parameters.put("id", rdbmsId.getId());

        return parameters;
    }

    private String convertIdsToCommaSeparatedString(List<Id> ids) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Id id : ids) {
            RdbmsId rdbmsId = (RdbmsId) id;
            builder.append(rdbmsId.getId());
            if (index < ids.size() - 1) {
                builder.append(", ");
            }
            index++;
        }
        return builder.toString();
    }

    private String getDomainObjectType(List<Id> ids) {
        String typeName = null;
        for (Id id : ids) {
            RdbmsId rdbmsId = (RdbmsId) id;
            typeName = rdbmsId.getTypeName();
            break;
        }

        return typeName;
    }

    /**
     * Проверяет какого типа идентификатор
     */
    private void validateIdType(Id id) {
        if (id == null) {
            throw new InvalidIdException(id);
        }
        if (!(id instanceof RdbmsId)) {
            throw new InvalidIdException(id);
        }
    }

    private void validateParentIdType(DomainObject domainObject, DomainObjectTypeConfig config) {
        if(domainObject.getParent() == null) {
            return;
        }

        RdbmsId id = (RdbmsId) domainObject.getParent();
        String idType = id.getTypeName();
        String parentName = config.getParentConfig() != null ? config.getParentConfig().getName() : null;

        if(!idType.equals(parentName)) {
            String errorMessage = "Invalid parent id type: expected '" + parentName + "' but was '" + idType + "'";
            throw new InvalidIdException(errorMessage, id);
        }
    }

    private void initializeDomainParameters(DomainObject domainObject, List<FieldConfig> fieldConfigs,
                                            Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : fieldConfigs) {
            Value value = domainObject.getValue(fieldConfig.getName());
            String columnName = DataStructureNamingHelper.getSqlName(fieldConfig.getName());
            String parameterName = DaoUtils.generateParameter(columnName);
            if (value != null) {
                parameters.put(parameterName, value.get());
            } else {
                parameters.put(parameterName, null);
            }

        }
    }

    private Long getParentId(DomainObject domainObject) {
        if(domainObject.getParent() == null) {
            return null;
        }

        RdbmsId rdbmsParentId = (RdbmsId) domainObject.getParent();
        return rdbmsParentId.getId();
    }

}
