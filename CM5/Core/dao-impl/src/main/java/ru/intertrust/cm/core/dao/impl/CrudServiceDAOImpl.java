package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.impl.utils.StrUtils;

public class CrudServiceDAOImpl implements CrudServiceDAO {

	private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник соединений
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
	
	
	private List<String> getFieldConfigNames(BusinessObjectConfig businessObjectConfig) {
		List<String> fildConfigNames = new ArrayList<String>();
		for (FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
			fildConfigNames.add(fieldConfig.getName());
		}
		return fildConfigNames;
	}
	
	
	private List<String> getFieldNames(BusinessObjectConfig businessObjectConfig) {
		List<String> fildConfigNames = new ArrayList<String>();
		for (FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
			fildConfigNames.add(fieldConfig.getName());
		}
		return fildConfigNames;
	}	

	
	public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
		
		StringBuilder query = new StringBuilder();
		String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase(); 
		String commaSeparatedFields = StrUtils.generateCommaSeparatedList(getFieldConfigNames(businessObjectConfig), true);
		String commaSeparatedParameters = StrUtils.generateCommaSeparatedList(getFieldConfigNames(businessObjectConfig),":", true);
		
		query.append("insert into ").append(tableName).append(" (");
		query.append(":id , :created_date, :updated_date, ").
		append(commaSeparatedFields);
        query.append(") values (");
        query.append(commaSeparatedParameters);
        query.append(")");
        
        Map parameters = new HashMap();
        parameters.put(":id", businessObject.getId());
        parameters.put(":created_date", businessObject.getCreatedDate());
        
        for(String field : businessObject.getFields()) {
        	Value value =  businessObject.getValue(field);
        	parameters.put(":" + field, value.get());
        	
        }
        
        jdbcTemplate.update(query.toString(), parameters);


        return null;
	}
	

	public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

		StringBuilder query = new StringBuilder();
		
		String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();
		
		String fieldsWithparams = StrUtils.generateCommaSeparatedListWithParams(getFieldConfigNames(businessObjectConfig), getFieldConfigNames(businessObjectConfig));
		
        query.append("update ").append(tableName).append(" set ");
        query.append("updated_date=:updated_date ");
        query.append(fieldsWithparams);
        query.append(" where id=:id");

        Map parameters = new HashMap();
        parameters.put(":id", businessObject.getId());
        parameters.put(":updated_date", businessObject.getModifiedDate());
        
        for(String field : businessObject.getFields()) {
        	Value value =  businessObject.getValue(field);
        	parameters.put(":" + field, value.get());
        	
        }
        

        jdbcTemplate.update(query.toString(), parameters);
        
        return null;

	}

	public BusinessObject read(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
		return null;
	}
		


}
