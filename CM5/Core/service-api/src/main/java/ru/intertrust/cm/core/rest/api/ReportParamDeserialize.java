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
                if (reportParamType == ReportParam.ParamTypes.List){
                    List<Object> listValue = new ArrayList<>();
                    for (Iterator<JsonNode> itItem = ((ArrayNode) param.get(ReportParam.VALUE)).iterator(); itItem.hasNext(); ) {
                        TreeNode paramItem = itItem.next();
                        String typeItem = ((TextNode)paramItem.get(ReportParam.TYPE)).asText();
                        String valueItem = ((TextNode)paramItem.get(ReportParam.VALUE)).asText();
                        listValue.add(getParamValue(typeItem, valueItem));
                    }
                    reportParamMap.put(name, listValue);
                }else{
                    String value = ((TextNode)param.get(ReportParam.VALUE)).asText();
                    reportParamMap.put(name, getParamValue(type, value));
                }
            }
            result.setParams(reportParamMap);
        }
        return result;
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
