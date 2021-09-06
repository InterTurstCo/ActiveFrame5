package ru.intertrust.cm.core.rest.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.model.FatalException;

import java.io.IOException;
import java.util.*;

public class ReportParamDeserialize extends JsonDeserializer<GenerateReportParam> {
    @Override
    public GenerateReportParam deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        GenerateReportParam result = new GenerateReportParam();
        TreeNode treeNode = parser.readValueAsTree();
        result.setName(((TextNode)treeNode.get(ReportParam.NAME)).asText());
        TreeNode params = treeNode.get(ReportParam.PARAMETERS);
        if (params != null){
            Map<String, Object> reportParamMap = new HashMap<>();
            for (Iterator<JsonNode> it = ((ArrayNode) params).iterator(); it.hasNext(); ) {
                TreeNode param = it.next();
                String type = ((TextNode)param.get(ReportParam.TYPE)).asText();
                String name = ((TextNode)param.get(ReportParam.NAME)).asText();
                ReportParam.ParamTypes reportParamType = ReportParam.ParamTypes.valueOf(type);
                List<Object> listValue = null;
                Map<Object, Object> mapValue = null;
                if ((listValue = deserializeAsList(reportParamType, param))!= null) {
                    reportParamMap.put(name, listValue);
                } else if ((mapValue = deserializeAsMap(reportParamType, param)) != null) {
                    reportParamMap.put(name, mapValue);
                } else {
                    String value = ((TextNode)param.get(ReportParam.VALUE)).asText();
                    reportParamMap.put(name, getParamValue(type, value));
                }
            }
            result.setParams(reportParamMap);
        }
        return result;
    }

    private List<Object> deserializeAsList(ReportParam.ParamTypes reportParamType, TreeNode param) {
        List<Object> listValue = null;
        if (reportParamType == ReportParam.ParamTypes.List){
            listValue = new ArrayList<>();
            for (Iterator<JsonNode> itItem = ((ArrayNode) param.get(ReportParam.VALUE)).iterator(); itItem.hasNext();) {
                TreeNode paramItem = itItem.next();
                String typeItem = ((TextNode)paramItem.get(ReportParam.TYPE)).asText();
                ReportParam.ParamTypes reportParamItemType = ReportParam.ParamTypes.valueOf(typeItem);
                List<Object> listItemValue = null;
                Map<Object, Object> mapItemValue = null;
                if ((listItemValue = deserializeAsList(reportParamItemType, paramItem)) != null) {
                    listValue.add(listItemValue);
                } else if ((mapItemValue = deserializeAsMap(reportParamItemType, paramItem)) != null) {
                    listValue.add(mapItemValue);
                } else {
                    String valueItem = ((TextNode)paramItem.get(ReportParam.VALUE)).asText();
                    listValue.add(getParamValue(typeItem, valueItem));
                }
            }
        }
        return listValue;
    }

    private Map<Object, Object> deserializeAsMap(ReportParam.ParamTypes reportParamType, TreeNode param) {
        Map<Object, Object> mapValue = null;
        if (reportParamType == ReportParam.ParamTypes.Map){
            mapValue = new LinkedHashMap<>();
            for (Iterator<JsonNode> itItem = ((ArrayNode) param.get(ReportParam.VALUE)).iterator(); itItem.hasNext();) {
                TreeNode paramItem = itItem.next();
                String typeItem = ((TextNode)paramItem.get(ReportParam.TYPE)).asText();
                String key = ((TextNode)paramItem.get(ReportParam.MAPKEY)).asText();
                ReportParam.ParamTypes reportParamItemType = ReportParam.ParamTypes.valueOf(typeItem);
                List<Object> listItemValue = null;
                Map<Object, Object> mapItemValue = null;
                if ((listItemValue = deserializeAsList(reportParamItemType, paramItem)) != null) {
                    mapValue.put(key, listItemValue);
                } else if ((mapItemValue = deserializeAsMap(reportParamItemType, paramItem)) != null) {
                    mapValue.put(key, mapItemValue);
                } else {
                    String valueItem = ((TextNode)paramItem.get(ReportParam.VALUE)).asText();
                    mapValue.put(key, getParamValue(typeItem, valueItem));
                }
            }
        }
        return mapValue;
    }

    private Object getParamValue(String type, String value){
        try {
            Object result = null;
            ReportParam.ParamTypes reportType = ReportParam.ParamTypes.valueOf(type);
            if (reportType == ReportParam.ParamTypes.String) {
                result = value;
            } else if (reportType == ReportParam.ParamTypes.Long) {
                result = Long.valueOf(value);
            } else if (reportType == ReportParam.ParamTypes.Int) {
                result = Integer.valueOf(value);
            } else if (reportType == ReportParam.ParamTypes.Double) {
                result = Double.valueOf(value);
            } else if (reportType == ReportParam.ParamTypes.Boolean) {
                result = Boolean.valueOf(value);
            } else if (reportType == ReportParam.ParamTypes.Calendar) {
                result = Calendar.getInstance();
                ((Calendar)result).setTime(ReportParam.format.parse(value));
            } else if (reportType == ReportParam.ParamTypes.DateTime) {
                result = ReportParam.format.parse(value);
            } else if (reportType == ReportParam.ParamTypes.Id) {
                result = new RdbmsId(value);
            } else {
                throw new UnsupportedOperationException("Unsupported parameter with type " + type);
            }
            return result;
        }catch(Exception ex){
            throw new FatalException("Error convert param value", ex);
        }
    }
}
