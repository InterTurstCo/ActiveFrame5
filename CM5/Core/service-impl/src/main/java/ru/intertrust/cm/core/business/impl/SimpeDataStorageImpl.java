package ru.intertrust.cm.core.business.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.SimpeDataStorage;
import ru.intertrust.cm.core.business.api.simpledata.SimpleData;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.simpledata.SimpleSearchOrder;
import ru.intertrust.cm.core.business.api.simpledata.SumpleSearchOrderDirection;
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
    @Autowired
    private SolrServer solrServer;
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    Map<String, SimpleDataFieldType> fieldTypes = new HashMap<>();

    @Override
    public void store(SimpleData data) {
        try {
            SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, data.getType());
            if (config == null){
                throw new FatalException("Simple data config with name " + data.getType() + " not found");
            }

            UpdateRequest request = new UpdateRequest();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("type", data.getType());
            for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                if (data.getValues(fieldConfig.getName()) != null) {
                    List<Object> values = new ArrayList<>();
                    for (Value dataValue : data.getValues(fieldConfig.getName())) {
                        values.add(dataValue.get());
                    }
                    doc.addField(fieldConfig.getName(), values);
                }
            }

            request.add(doc);
            solrServer.request(request);
            solrServer.commit();
        }catch (Exception ex){
            throw new FatalException("Error save simple data", ex);
        }
    }

    @Override
    public List<SimpleData> find(Map<String, Value> filter, List<String> resultFields, List<SimpleSearchOrder> resultOrder) {
        try{
            SolrQuery query = new SolrQuery();
            query.addFilterQuery(createQuery(filter));

            if (!resultFields.contains("type")){
                query.addField("type");
            }
            for (String resultField : resultFields) {
                query.addField(resultField);
            }

            for (SimpleSearchOrder order :resultOrder) {
                query.addSort(order.getFieldName(),
                        order.getDirection() != null && order.getDirection() == SumpleSearchOrderDirection.DESK ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc);
            }

            QueryResponse response = solrServer.query(query);
            SolrDocumentList documentList = response.getResults();
            List<SimpleData> result = new ArrayList<>();
            for (SolrDocument document : documentList) {
                SimpleData resultItem = new SimpleData();
                resultItem.setType((String)document.getFieldValue("type"));
                for (String resultField : resultFields) {
                    resultItem.addValues(resultField, getFieldValue(resultItem.getType(), resultField, document.getFieldValues(resultField)));
                }
            }
            return result;
        }catch (Exception ex){
            throw new FatalException("Error search simple data", ex);
        }
    }

    private Value[] getFieldValue(String type, String name, Collection<Object> fieldValues) {
        Value[] result = new Value[fieldValues.size()];
        SimpleDataConfig config = configurationExplorer.getConfig(SimpleDataConfig.class, type);
        SimpleDataFieldType fieldType = getFieldType(config, name);
        int i=0;
        for (Object fieldValue : fieldValues) {
            if (fieldType.equals(SimpleDataFieldType.String)){
                result[i] = new StringValue((String)fieldValue);
            }else if (fieldType.equals(SimpleDataFieldType.Long)){
                result[i] = new LongValue((Long)fieldValue);
            }else if (fieldType.equals(SimpleDataFieldType.Bollean)){
                result[i] = new BooleanValue((Boolean)fieldValue);
            }else if (fieldType.equals(SimpleDataFieldType.DateTime)){
                result[i] = new DateTimeValue((Date)fieldValue);
            }else if (fieldType.equals(SimpleDataFieldType.Date)){
                String[] dateValue = ((String)fieldValue).split("-");
                TimelessDate timelessDate = new TimelessDate(Integer.parseInt(dateValue[0]),
                        Integer.parseInt(dateValue[1]), Integer.parseInt(dateValue[2]));
                result[i] = new TimelessDateValue(timelessDate);
            }
            i++;
        }

        return result;
    }

    /**
     * Кэшируем тип полей
     * @param config
     * @param name
     * @return
     */
    private SimpleDataFieldType getFieldType(SimpleDataConfig config, String name) {
        SimpleDataFieldType result = fieldTypes.get(config.getName().toLowerCase() + ":" + name.toLowerCase());
        if (result == null){
            for (SimpleDataFieldConfig fieldConfig : config.getFields()) {
                fieldTypes.put(config.getName().toLowerCase() + ":" + name.toLowerCase(), fieldConfig.getType());
            }
            result = fieldTypes.get(config.getName().toLowerCase() + ":" + name.toLowerCase());
        }
        return result;
    }

    @Override
    public void delete(Map<String, Value> filter) {
        try{
            UpdateResponse response = solrServer.deleteByQuery(createQuery(filter));
            solrServer.commit();
        }catch (Exception ex){
            throw new FatalException("Error save simple data", ex);
        }
    }

    private String createQuery(Map<String, Value> filter){
        String result = "";
        for (String fieldName : filter.keySet()) {
            Value value = filter.get(fieldName);
            if (!result.isEmpty()){
                result += " AND ";
            }
            if (value instanceof StringValue){
                result += fieldName + ":\"" + value.get() + "\"";
            }else if (value instanceof LongValue){
                result += fieldName + ":" + value.get() + "";
            }else if (value instanceof BooleanValue){
                result += fieldName + ":" + value.get() + "";
            }else if (value instanceof DateTimeValue){
                result += fieldName + ":\"" + dateTimeFormat.format(value.get()) + "\"";
            }else if (value instanceof TimelessDateValue){
                TimelessDate timelessDate = ((TimelessDateValue)value).get();
                result += fieldName + ":\"" + timelessDate.toString() + "\"";
            }
        }
        return result;
    }
}
