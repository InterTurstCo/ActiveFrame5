package ru.intertrust.cm.core.business.impl;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.SimpeDataStorage;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.simpledata.*;
import ru.intertrust.cm.core.business.impl.search.simple.SimpleDataSearchFilterQueryFactory;
import ru.intertrust.cm.core.business.impl.search.simple.SimpleSearchUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(SimpeDataStorageImpl.class);

    @Autowired
    private SolrClient solrServer;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private SimpleSearchUtils simpleSearchUtils;
    @Autowired
    private SimpleDataSearchFilterQueryFactory queryFactory;

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
                    for (Value<?> dataValue : data.getValues(fieldConfig.getName())) {
                        values.add(dataValue.get());
                    }
                    doc.addField(simpleSearchUtils.getSolrFieldName(config, fieldConfig.getName()), values);
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
    public List<SimpleData> find(String type, List<SimpleDataSearchFilter> filters, List<String> resultFields, List<SimpleSearchOrder> resultOrder, int limit) {
        try {
            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, type);
            SolrQuery query = new SolrQuery();

            if (limit > -1) {
                query.setRows(limit);
            }

            query.setQuery(createQuery(config, filters));

            query.addField("id");
            if (resultFields != null) {
                for (String resultField : resultFields) {
                    query.addField(simpleSearchUtils.getSolrFieldName(config, resultField));
                }
            }

            if (resultOrder != null) {
                for (SimpleSearchOrder order : resultOrder) {
                    query.addSort(simpleSearchUtils.getSolrFieldName(config, order.getFieldName()),
                            order.getDirection() != null && order.getDirection() == SumpleSearchOrderDirection.DESС ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Execute query: {}", query);
            }
            QueryResponse response = solrServer.query(query);
            SolrDocumentList documentList = response.getResults();
            if (logger.isDebugEnabled()) {
                logger.debug("Execute query result size: {}", documentList.size());
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

    @Override
    public List<SimpleData> find(String type, List<SimpleDataSearchFilter> filters, List<String> resultFields, List<SimpleSearchOrder> resultOrder) {
        return find(type, filters, resultFields, resultOrder, -1);
    }

    private Value<?>[] getFieldValue(SimpleDataConfig config, String fieldName, SolrDocument document) {
        Collection<Object> fieldValues = document.getFieldValues(simpleSearchUtils.getSolrFieldName(config, fieldName));
        Value<?>[] result = null;
        if (fieldValues != null) {
            result = new Value[fieldValues.size()];
            SimpleDataFieldConfig fieldConfig = simpleSearchUtils.getFieldConfig(config, fieldName);
            int i = 0;
            for (Object fieldValue : fieldValues) {
                if (fieldConfig.getType().equals(SimpleDataFieldType.String)) {
                    result[i] = new StringValue((String) fieldValue);
                } else if (fieldConfig.getType().equals(SimpleDataFieldType.Long)) {
                    result[i] = new LongValue((Long) fieldValue);
                } else if (fieldConfig.getType().equals(SimpleDataFieldType.Bollean)) {
                    result[i] = new BooleanValue((Boolean) fieldValue);
                } else if (fieldConfig.getType().equals(SimpleDataFieldType.DateTime)) {
                    result[i] = new DateTimeValue((Date) fieldValue);
                } else if (fieldConfig.getType().equals(SimpleDataFieldType.Date)) {
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
                query.addField(simpleSearchUtils.getSolrFieldName(config, fieldConfig.getName()));
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
        StringBuilder result = new StringBuilder("cm_type: \"" + config.getName() + "\"");
        if (filters != null) {
            for (SimpleDataSearchFilter filter : filters) {
                result.append(" AND ").append(queryFactory.getQuery(config, filter));
            }
        }
        return result.toString();
    }

}
