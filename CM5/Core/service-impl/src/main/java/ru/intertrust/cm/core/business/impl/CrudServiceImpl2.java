package ru.intertrust.cm.core.business.impl;

import java.math.BigDecimal;
import java.rmi.ServerException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IntegerValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.CollectionFilterConfig;
import ru.intertrust.cm.core.config.CollectionFilterCriteria;
import ru.intertrust.cm.core.config.CollectionFilterReference;


public class CrudServiceImpl2 implements CrudService {

    private static final String EMPTY_PLACEHOLDER = " ";

    private static final String CRITERIA_PLACEHOLDER = "::where-clause";

    private static final String REFERENCE_PLACEHOLDER = "::from-clause";

    private static final String SQL_DESCENDING_ORDER = "desc";

    private static final String SQL_ASCENDING_ORDER = "asc";

    public static final String QUERY_FILTER_PARAM_DELIMETER = ":";
    
    public static final String DEFAULT_CRITERIA_CONDITION = "and";
    
    private JdbcTemplate jdbcTemplate;

    /**
     * Устанавливает {@link #jdbcTemplate}
     * @param dataSource DataSource для инициализации {@link #jdbcTemplate}
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
        
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public IdentifiableObject createIdentifiableObject() {
        // TODO Auto-generated method stub
        return null;
    }

    public BusinessObject createBusinessObject(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public BusinessObject save(BusinessObject businessObject) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<BusinessObject> save(List<BusinessObject> businessObjects) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean exists(Id id) {
        // TODO Auto-generated method stub
        return false;
    }

    public BusinessObject find(Id id) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<BusinessObject> find(List<Id> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    public IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
         CollectionConfiguration collectionsConfiguration = null;
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        try {
            collectionsConfiguration = configurationLoader.serializeCollectionConfiguration("config/collections.xml");
        } catch (Exception e) {
            throw new RuntimeException("Exception loading configuration");
        }
        
        CollectionConfig collectionConfig = collectionsConfiguration.findCollectionConfigByName(collectionName);

        List<CollectionFilterConfig> filledFilterConfigs = findFilledFilterConfigs(filterValues, collectionConfig);

        String prototypeQuery = mergeFilledFilterConfigsInPrototypeQuery(collectionConfig.getPrototype(), filledFilterConfigs);
        
        prototypeQuery = applySortOrder(sortOrder, prototypeQuery);        
        
        return findByQuery(prototypeQuery, collectionConfig.getBusinessObjectTypeField(), collectionConfig.getIdField(), 1, 0, 0);
        
    }


    private List<CollectionFilterConfig> findFilledFilterConfigs(List<Filter> filterValues, CollectionConfig collectionConfig) {
        List<CollectionFilterConfig> filterConfigs = collectionConfig.getFilters();        
        
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<CollectionFilterConfig>();
        
        //validate filters
        
        for (CollectionFilterConfig filterConfig : filterConfigs) {
            for (Filter filterValue : filterValues) {
                if (!filterConfig.getName().equals(filterValue.getFilter())) {
                    continue;
                }
                CollectionFilterConfig filledFilterConfig = replaceFilterCriteriaParam(filterConfig, filterValue);
                filledFilterConfigs.add(filledFilterConfig);
                
            }
        }
        return filledFilterConfigs;
    }
    
    private String applySortOrder(SortOrder sortOrder, String prototypeQuery) {
        StringBuilder prototypeQueryBuilder = new StringBuilder(prototypeQuery);
        
        boolean hasSortEntry = false;
        if(sortOrder != null && sortOrder.size() > 0) {
            for(SortCriterion criterion : sortOrder){
                if(hasSortEntry){
                    prototypeQueryBuilder.append(", ");
                }
                prototypeQueryBuilder.append(" order by ").append(criterion.getField()).append("  ").append(getSqlSortOrder(criterion.getOrder()));
                hasSortEntry = true;
            }
        }
        return prototypeQueryBuilder.toString();
    }
    
    private String mergeFilledFilterConfigsInPrototypeQuery(String prototypeQuery, List<CollectionFilterConfig> filledFilterConfigs) {
        StringBuilder mergedFilterCriteria = new StringBuilder();
        StringBuilder mergedFilterReference = new StringBuilder();
        
        boolean hasEntry = false;
        for(CollectionFilterConfig collectionFilterConfig : filledFilterConfigs){
            
            if (collectionFilterConfig.getFilterReference() != null) {
                mergedFilterReference.append(collectionFilterConfig.getFilterReference().getValue());
            }
            if (hasEntry) {
                mergedFilterCriteria.append(EMPTY_PLACEHOLDER);
                if (collectionFilterConfig.getFilterCriteria().getCondition() != null) {
                    mergedFilterCriteria.append(collectionFilterConfig.getFilterCriteria().getCondition());
                    
                } else {
                    mergedFilterCriteria.append(DEFAULT_CRITERIA_CONDITION);
                    
                }
                mergedFilterCriteria.append(EMPTY_PLACEHOLDER);                                
            }
            mergedFilterCriteria.append(collectionFilterConfig.getFilterCriteria().getValue());
            hasEntry = true;
        }
        
        prototypeQuery = applyMergedFilterReference(prototypeQuery, mergedFilterReference.toString());
        
        prototypeQuery = applyMergedFilterCriteria(prototypeQuery, mergedFilterCriteria.toString());
        return prototypeQuery;
    }
    
    private String applyMergedFilterCriteria(String prototypeQuery, String mergedFilterCriteria) {
        if (mergedFilterCriteria.length() > 0) {
            prototypeQuery = prototypeQuery.replaceAll(CRITERIA_PLACEHOLDER, mergedFilterCriteria);
        } else {
            prototypeQuery = prototypeQuery.replaceAll(CRITERIA_PLACEHOLDER, EMPTY_PLACEHOLDER);
        }
        return prototypeQuery;
    }
    
    private String applyMergedFilterReference(String prototypeQuery, String mergedFilterReference) {
        if (mergedFilterReference.length() > 0) {
            prototypeQuery = prototypeQuery.replaceAll(REFERENCE_PLACEHOLDER, mergedFilterReference);
        } else {
            prototypeQuery = prototypeQuery.replaceAll(REFERENCE_PLACEHOLDER, EMPTY_PLACEHOLDER);
        }
        return prototypeQuery;
    }
    
    
    private String getSqlSortOrder(SortCriterion.Order order) {
        if (order == Order.ASCENDING) {
            return SQL_ASCENDING_ORDER;
        } else if (order == Order.DESCENDING) {
            return SQL_DESCENDING_ORDER;
        } else {
            return SQL_ASCENDING_ORDER;
        }
    }    
    
    private CollectionFilterConfig replaceFilterCriteriaParam(CollectionFilterConfig filterConfig, Filter filterValue) {
        CollectionFilterConfig clonedFilterConfig = cloneFilterConfig(filterConfig);
        int index = 0;
        
        for (String value : filterValue.getValues()) {
            
            String criteria = clonedFilterConfig.getFilterCriteria().getValue();
            
            String paramName = ":" + index;
            
            String newFilterCriteria = criteria.replaceAll(paramName, value);
            
            clonedFilterConfig.getFilterCriteria().setValue(newFilterCriteria);
            index++;
            
        }
        
        return clonedFilterConfig;
    }
    
    /**
     * Клонирует конфигурацию коллекции. При заполнении параметров в фильтрах нужно, чтобы первоначальная конфигурация коллекции оставалась неизменной.
     * @param filterConfig конфигурации коллекции
     * @return копия переданной конфигурации коллекции
     */
    private CollectionFilterConfig cloneFilterConfig(CollectionFilterConfig filterConfig) {
        CollectionFilterConfig clonedFilterConfig = new CollectionFilterConfig();
        
        CollectionFilterReference srcFilterReference = filterConfig.getFilterReference();
        if (srcFilterReference != null) {
            CollectionFilterReference clonedFilterReference = new CollectionFilterReference();
            clonedFilterReference.setPlaceholder(srcFilterReference.getPlaceholder());
            clonedFilterReference.setValue(srcFilterReference.getValue());
            clonedFilterConfig.setFilterReference(clonedFilterReference);
        }
        
        CollectionFilterCriteria srcFilterCriteria = filterConfig.getFilterCriteria();
        if (srcFilterCriteria != null) {
            CollectionFilterCriteria clonedFilterCriteria = new CollectionFilterCriteria();
            clonedFilterCriteria.setPlaceholder(srcFilterCriteria.getPlaceholder());
            clonedFilterCriteria.setCondition(srcFilterCriteria.getCondition());
            clonedFilterCriteria.setValue(srcFilterCriteria.getValue());
            clonedFilterConfig.setFilterCriteria(clonedFilterCriteria);
        }
        
        clonedFilterConfig.setName(filterConfig.getName());
        
        return clonedFilterConfig;
    }
    public IdentifiableObjectCollection findByQuery(String query, String objectType, String idField, long userId, int limit, int offset) {
        if (limit != 0) {
            query += " limit " + limit + " OFFSET " + offset;
        }
        
        IdentifiableObjectCollection collection = (IdentifiableObjectCollection) getJdbcTemplate().query(query, new CollectionRowMapper(objectType, idField));
        System.out.print(collection.getFields() + "\n");
        
        System.out.print(collection.toString());

        return collection;
    }

    
    public int findCollectionCount(String collectionName, List<Filter> filters, SortOrder sortOrder) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void delete(Id id) {
        // TODO Auto-generated method stub

    }

    public int delete(Collection<Id> ids) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int deleteAll(String businessObjectName) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public static void main(String [] args) throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.xa.PGXADataSource");
        dataSource.setUrl("dbc:postgresql://localhost:5432/br4j22");
        dataSource.setUsername("br4j");
        dataSource.setPassword("welcome");
        
        CrudServiceImpl2 crudServiceImpl = new CrudServiceImpl2();
        crudServiceImpl.setDataSource(dataSource);
        List<Filter> filterValues = new ArrayList<Filter>();
        Filter filerValue = new Filter();
        List<String> values = new ArrayList<>();
        values.add("dep1");
        filerValue.setValues(values);
        filerValue.setFilter("byDepartment");
        
        Filter byNameFilter = new Filter();
        List<String> byNameValues = new ArrayList<>();
        byNameValues.add("employee2");
        
        byNameFilter.setValues(byNameValues);
        byNameFilter.setFilter("byName");

        filterValues.add(filerValue);
        filterValues.add(byNameFilter);
        crudServiceImpl.findCollection("Employees", filterValues, null, 0, 0 );
    }

    @SuppressWarnings("rawtypes")
    private class CollectionRowMapper implements ResultSetExtractor {

        private String businessObjectType;

        private String idField;
                
        
        public CollectionRowMapper(String businessObjectType, String idField) {
            this.businessObjectType = businessObjectType;
            this.idField = idField;
        }

        @Override
        public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
            IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

            ColumnModel columnModel = new ColumnModel();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String fieldName = rs.getMetaData().getColumnName(i);
                DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
                if (fieldName.equals(idField)) {
                    columnModel.setIdField(fieldName);
                    columnModel.getColumnTypes().add(DataType.ID);
                } else {
                    columnModel.getColumnNames().add(fieldName);
                    columnModel.getColumnTypes().add(fieldType);
                }

            }

            collection.setFields(columnModel.getColumnNames());

            int row = 0;
            while (rs.next()) {
                int index = 0;
                int collectionIndex = 0;

                Id id = null;
                for (DataType fieldType : columnModel.getColumnTypes()) {
                    Value value = null;
                    if (DataType.ID.equals(fieldType)) {

                        Long longValue = rs.getLong(columnModel.getIdField());
                        if (!rs.wasNull()) {
                            id = new RdbmsId(businessObjectType, longValue);
                        } else {
                            throw new RuntimeException("Id field can not be null for object " + "business_object");
                        }

                    } else if (DataType.INTEGER.equals(fieldType)) {
                        value = new DecimalValue();
                        Long longValue = rs.getLong(index + 1);
                        if (!rs.wasNull()) {
                            value = new IntegerValue(longValue);
                        } else {
                            value = new IntegerValue();
                        }

                    } else if (DataType.DATETIME.equals(fieldType)) {
                        Timestamp timestamp = rs.getTimestamp(index + 1);
                        if (!rs.wasNull()) {
                            Date date = new Date(timestamp.getTime());
                            value = new TimestampValue(date);
                        } else {
                            value = new TimestampValue();
                        }

                    } else if (DataType.STRING.equals(fieldType)) {
                        String fieldValue = rs.getString(index + 1);
                        if (!rs.wasNull()) {
                            value = new StringValue(fieldValue);
                        } else {
                            value = new StringValue();
                        }

                    } else if (DataType.BOOLEAN.equals(fieldType)) {
                        Boolean fieldValue = rs.getBoolean(index + 1);
                        if (!rs.wasNull()) {
                            value = new BooleanValue(fieldValue);
                        } else {
                            value = new BooleanValue();
                        }

                    } else if (DataType.DECIMAL.equals(fieldType)) {
                        BigDecimal fieldValue = rs.getBigDecimal(index + 1);
                        if (!rs.wasNull()) {
                            value = new DecimalValue(fieldValue);
                        } else {
                            value = new DecimalValue();
                        }
                    }
                    collectionIndex = index;

                    if (id != null) {
                        collection.setId(row, id);
                        collectionIndex = index == 0 ? 0 : index - 1;
                    }
                    if (value != null) {
                        collection.set(collectionIndex, row, value);
                    }
                    index++;
                }

                row++;
            }
            return collection;
        }

        private DataType getColumnType(String columnTypeName) {
            DataType result = null;
            if (columnTypeName.equals("int8")) {
                result = DataType.INTEGER;
            } else if (columnTypeName.equals("timestamp")) {
                result = DataType.DATETIME;
            } else if (columnTypeName.equals("varchar") || columnTypeName.equals("unknown") || columnTypeName.equals("text")) {
                result = DataType.STRING;
            } else if (columnTypeName.equals("bool")) {
                result = DataType.BOOLEAN;
            } else if (columnTypeName.equals("numeric")) {
                result = DataType.DECIMAL;
            }
            return result;
        }       

        /**
         * Метаданные возвращаемых значений списка. Содержит названия колонок и их типы.
         * @author atsvetkov
         * 
         */
        private class ColumnModel {

            private String idField;

            private List<String> columnNames;

            private List<DataType> columnTypes;

            public List<String> getColumnNames() {
                if (columnNames == null) {
                    columnNames = new ArrayList<String>();
                }
                return columnNames;
            }

            public List<DataType> getColumnTypes() {
                if (columnTypes == null) {
                    columnTypes = new ArrayList<DataType>();
                }
                return columnTypes;
            }

            public String getIdField() {
                return idField;
            }

            public void setIdField(String idField) {
                this.idField = idField;
            }
        }

    }

    /**
     * Перечисление типов колонок в таблицах бизнес-объектов. Используется для удобства чтения полей бизнес-объектов.
     * @author atsvetkov
     * 
     */
    private enum DataType {
        STRING("string"), INTEGER("int"), DECIMAL("decimal"), DATETIME("datetime"), BOOLEAN("boolean"), ID("id");

        private final String value;

        DataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
        
}
