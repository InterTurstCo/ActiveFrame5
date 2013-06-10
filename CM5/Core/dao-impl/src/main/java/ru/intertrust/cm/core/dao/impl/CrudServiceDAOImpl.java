package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
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


	public int generateNextSequence() {

		String query = "select nextval ('business_object_seq')";
		Integer id = jdbcTemplate.queryForObject(query, new HashMap<String, Object>(), Integer.class);

		return id.intValue();

	}

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

	public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

		StringBuilder query = new StringBuilder();

		String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

		String fieldsWithparams = StrUtils.generateCommaSeparatedListWithParams(businessObject.getFields(), businessObject.getFields());

		query.append("update ").append(tableName).append(" set ");
		query.append("updated_date=:updated_date, ");
		query.append(fieldsWithparams);
		query.append(" where id=:id");

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

	public BusinessObject read(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
		return null;
	}

}
