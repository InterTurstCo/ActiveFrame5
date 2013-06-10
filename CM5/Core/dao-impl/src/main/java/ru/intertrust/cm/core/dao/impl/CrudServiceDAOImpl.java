package ru.intertrust.cm.core.dao.impl;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.impl.utils.StrUtils;

public class CrudServiceDAOImpl implements CrudServiceDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public int generateNextSequence() {

        String query = "select nextval ('business_object_seq')";
        Integer id = jdbcTemplate.queryForObject(query, new HashMap<String, Object>(), Integer.class);

        return id.intValue();

    }

    @Override
    public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        StringBuilder query = new StringBuilder();
        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();
        String commaSeparatedFields = StrUtils.generateCommaSeparatedList(businessObject.getFields(), true);
        String commaSeparatedParameters = StrUtils.generateCommaSeparatedList(businessObject.getFields(), ":", false);

        query.append("insert into ").append(tableName).append(" (");
        query.append("ID , CREATED_DATE, UPDATED_DATE, ").append(commaSeparatedFields);
        query.append(") values (");
        query.append(":id , :created_date, :updated_date, ");
        query.append(commaSeparatedParameters);
        query.append(")");

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        parameters.put("created_date", businessObject.getCreatedDate());
        parameters.put("updated_date", businessObject.getModifiedDate());

        for (String field : businessObject.getFields()) {
            Value value = businessObject.getValue(field);
            if (value != null)
                parameters.put(field, value.get());
            else
                parameters.put(field, null);

        }

        jdbcTemplate.update(query.toString(), parameters);

        return null;
    }

    @Override
    public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        String fieldsWithparams = StrUtils.generateCommaSeparatedListWithParams(businessObject.getFields(),
                businessObject.getFields());

        query.append("update ").append(tableName).append(" set ");
        query.append("updated_date=:updated_date, ");
        query.append(fieldsWithparams);
        query.append(" where id=:id");
        query.append(" and updated_date=:modified_date");

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        parameters.put("updated_date", businessObject.getModifiedDate());

        for (String field : businessObject.getFields()) {
            Value value = businessObject.getValue(field);
            parameters.put(field, value.get());

        }

        jdbcTemplate.update(query.toString(), parameters);

        return null;

    }

    @Override
    public void delete(Id id, BusinessObjectConfig businessObjectConfig) {

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        RdbmsId rdbmsId = (RdbmsId) id;

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        int count = jdbcTemplate.update(query.toString(), parameters);

        if (count == 0)
            throw new ObjectNotFoundException(rdbmsId);

    }




    @Override
    public boolean exists(Id id, BusinessObjectConfig businessObjectConfig) {

        RdbmsId rdbmsId = (RdbmsId)id;

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        Map<String, Long> parameters = new HashMap<String, Long>();
        parameters.put("id", rdbmsId.getId());


        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;

    }

    @Override
    public BusinessObject read(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
        return null;
    }

}
