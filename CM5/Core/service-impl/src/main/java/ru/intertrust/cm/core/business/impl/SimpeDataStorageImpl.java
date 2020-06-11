package ru.intertrust.cm.core.business.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.SimpeDataStorage;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.simpledata.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldType;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.text.SimpleDateFormat;
import java.util.*;

@Stateless(name = "SimpeDataStorage")
@Local(SimpeDataStorage.class)
@Remote(SimpeDataStorage.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class SimpeDataStorageImpl implements SimpeDataStorage {
    private Logger logger = LoggerFactory.getLogger(SimpeDataStorageImpl.class);

    @Autowired
    private SolrServer solrServer;
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    Map<String, SimpleDataFieldType> fieldTypes = new HashMap<>();

    @Override
    public void save(SimpleData data) {
        try {
            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, data.getType());
            if (config == null) {
                throw new FatalException("Simple data config with name " + data.getType() + " not found");
            }

            UpdateRequest request = new UpdateRequest();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("cm_type", data.getType());
            doc.addField("id", data.getId());
            for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                if (data.getValues(fieldConfig.getName()) != null) {
                    List<Object> values = new ArrayList<>();
                    for (Value dataValue : data.getValues(fieldConfig.getName())) {
                        values.add(dataValue.get());
                    }
                    doc.addField(getSolrFieldName(config, fieldConfig.getName()), values);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Save data: " + doc.toString());
            }
            request.add(doc);
            solrServer.request(request);
            solrServer.commit();

        } catch (Exception ex) {
            logger.error("Error save simple data", ex);
            throw new FatalException("Error save simple data", ex);
        }
    }

    @Override
    public List<SimpleData> find(String type, List<SimpleDataSearchFilter> filters, List<String> resultFields, List<SimpleSearchOrder> resultOrder) {
        try {
            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, type);
            SolrQuery query = new SolrQuery();
            query.setQuery(createQuery(config, filters));

            query.addField("id");
            if (resultFields != null) {
                for (String resultField : resultFields) {
                    query.addField(getSolrFieldName(config, resultField));
                }
            }

            if (resultOrder != null) {
                for (SimpleSearchOrder order : resultOrder) {
                    query.addSort(getSolrFieldName(config, order.getFieldName()),
                            order.getDirection() != null && order.getDirection() == SumpleSearchOrderDirection.DESK ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Execute query: " + query.toString());
            }
            QueryResponse response = solrServer.query(query);
            SolrDocumentList documentList = response.getResults();
            if (logger.isDebugEnabled()) {
                logger.debug("Execute query result size: " + documentList.size());
            }
            List<SimpleData> result = new ArrayList<>();
            for (SolrDocument document : documentList) {
                SimpleData resultItem = new SimpleData(type);
                resultItem.setId(document.getFieldValue("id").toString());
                if (resultFields != null) {
                    for (String resultField : resultFields) {
                        resultItem.addValues(resultField, getFieldValue(config, resultField, document));
                    }
                }
                result.add(resultItem);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Error search simple data", ex);
            throw new FatalException("Error search simple data", ex);
        }
    }

    private String getSolrFieldName(SimpleDataConfig config, String fieldName) {
        SimpleDataFieldType fieldType = getFieldType(config, fieldName);
        String result = null;
        if (fieldType.equals(SimpleDataFieldType.String)) {
            result = "cm_rs_" + fieldName;
        } else if (fieldType.equals(SimpleDataFieldType.Long)) {
            result = "cm_ls_" + fieldName;
        } else if (fieldType.equals(SimpleDataFieldType.Bollean)) {
            result = "cm_bs_" + fieldName;
        } else if (fieldType.equals(SimpleDataFieldType.DateTime)) {
            result = "cm_dts_" + fieldName;
        } else if (fieldType.equals(SimpleDataFieldType.Date)) {
            result = "cm_dts_" + fieldName;
        } else {
            throw new FatalException("Field " + fieldName + ". Type " + fieldType + " is not supported");
        }
        return result;
    }

    private Value[] getFieldValue(SimpleDataConfig config, String fieldName, SolrDocument document) {
        Collection<Object> fieldValues = document.getFieldValues(getSolrFieldName(config, fieldName));
        Value[] result = null;
        if (fieldValues != null) {
            result = new Value[fieldValues.size()];
            SimpleDataFieldType fieldType = getFieldType(config, fieldName);
            int i = 0;
            for (Object fieldValue : fieldValues) {
                if (fieldType.equals(SimpleDataFieldType.String)) {
                    result[i] = new StringValue((String) fieldValue);
                } else if (fieldType.equals(SimpleDataFieldType.Long)) {
                    result[i] = new LongValue((Long) fieldValue);
                } else if (fieldType.equals(SimpleDataFieldType.Bollean)) {
                    result[i] = new BooleanValue((Boolean) fieldValue);
                } else if (fieldType.equals(SimpleDataFieldType.DateTime)) {
                    result[i] = new DateTimeValue((Date) fieldValue);
                } else if (fieldType.equals(SimpleDataFieldType.Date)) {
                    Date dateValue = (Date) fieldValue;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateValue);

                    TimelessDate timelessDate = new TimelessDate(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    result[i] = new TimelessDateValue(timelessDate);
                }
                i++;
            }
        }

        return result;
    }

    /**
     * Кэшируем тип полей
     *
     * @param config
     * @param name
     * @return
     */
    private SimpleDataFieldType getFieldType(SimpleDataConfig config, String name) {
        SimpleDataFieldType result = fieldTypes.get(config.getName().toLowerCase() + ":" + name.toLowerCase());
        if (result == null) {
            for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                fieldTypes.put(config.getName().toLowerCase() + ":" + fieldConfig.getName().toLowerCase(),
                        fieldConfig.getType());
            }
            result = fieldTypes.get(config.getName().toLowerCase() + ":" + name.toLowerCase());
        }
        return result;
    }

    @Override
    public void delete(String type, List<SimpleDataSearchFilter> filters) {
        try {
            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, type);
            UpdateResponse response = solrServer.deleteByQuery(createQuery(config, filters));
            if (logger.isDebugEnabled()){
                logger.debug("Delete response " + response);
            }
            solrServer.commit();
        } catch (Exception ex) {
            logger.error("Error delete simple data", ex);
            throw new FatalException("Error save simple data", ex);
        }
    }

    @Override
    public SimpleData find(String type, String id) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("id: \"" + id + "\"");

            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, type);

            query.addField("id");
            for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                query.addField(getSolrFieldName(config, fieldConfig.getName()));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Execute query: " + query.toString());
            }
            QueryResponse response = solrServer.query(query);
            SolrDocumentList documentList = response.getResults();
            if (logger.isDebugEnabled()) {
                logger.debug("Execute query result size: " + documentList.size());
            }

            SimpleData result = null;
            if (documentList.size() > 0) {
                SolrDocument document = documentList.get(0);
                result = new SimpleData(type);
                result.setId(document.getFieldValue("id").toString());
                for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                    result.addValues(fieldConfig.getName(), getFieldValue(config, fieldConfig.getName(), document));
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("Error find simple data by id", ex);
            throw new FatalException("Error find simple data by id", ex);
        }
    }

    @Override
    public void delete(String id) {
        try {
            UpdateResponse response = solrServer.deleteById(id);
            if (logger.isDebugEnabled()){
                logger.debug("Delete response " + response);
            }
            solrServer.commit();
        } catch (Exception ex) {
            throw new FatalException("Error delete simple data", ex);
        }
    }

    private String createQuery(SimpleDataConfig config, List<SimpleDataSearchFilter> filters) {
        String result = "cm_type: \"" + config.getName() + "\"";

        if (filters != null) {
            for (SimpleDataSearchFilter filter : filters) {
                result += " AND " + getFilterQuery(config, filter);
            }
        }
        return result;
    }

    private String getFilterQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        if (filter instanceof EqualSimpleDataSearchFilter) {
            return getEqualSimpleDataSearchFilterQuery(config, (EqualSimpleDataSearchFilter) filter);
        } else if (filter instanceof LikeSimpleDataSearchFilter) {
            return getLikeSimpleDataSearchFilterQuery(config, (LikeSimpleDataSearchFilter) filter);
        }
        throw new FatalException("Filter " + filter.getClass() + " is not supported");
    }

    private String getLikeSimpleDataSearchFilterQuery(SimpleDataConfig config, LikeSimpleDataSearchFilter filter) {
        String solrFieldName = getSolrFieldName(config, filter.getFieldName());
        Value value = filter.getFieldValue();
        String result = null;
        if (value instanceof StringValue) {
            result = solrFieldName + ": *" + value.get() + "*";
        } else {
            throw new FatalException("Like filter can be use only with String fields");
        }
        return result;
    }

    private String getEqualSimpleDataSearchFilterQuery(SimpleDataConfig config, EqualSimpleDataSearchFilter filter) {
        String solrFieldName = getSolrFieldName(config, filter.getFieldName());
        Value value = filter.getFieldValue();
        String result = null;
        if (value instanceof StringValue) {
            result = solrFieldName + ": \"" + value.get() + "\"";
        } else if (value instanceof LongValue) {
            result = solrFieldName + ": " + value.get() + "";
        } else if (value instanceof BooleanValue) {
            result = solrFieldName + ": " + value.get() + "";
        } else if (value instanceof DateTimeValue) {
            result = solrFieldName + ": \"" + dateTimeFormat.format(value.get()) + "\"";
        } else if (value instanceof TimelessDateValue) {
            TimelessDate timelessDate = ((TimelessDateValue) value).get();
            result = solrFieldName + ": \"" + timelessDate.toString() + "\"";
        }
        return result;
    }
}
