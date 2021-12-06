package ru.intertrust.cm.core.rest.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportParamSerializer extends JsonSerializer<GenerateReportParam> {

    @Override
    public void serialize(GenerateReportParam value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        generator.writeStringField(ReportParam.NAME, value.getName());
        if (value.getParams() != null && value.getParams().size() > 0){
            generator.writeArrayFieldStart(ReportParam.PARAMETERS);
            for (String parameterName : value.getParams().keySet()) {
                generator.writeStartObject();
                generator.writeStringField(ReportParam.NAME, parameterName);
                Object repartParameterValue = value.getParams().get(parameterName);
                ParamValue paramValue = getParamValue(repartParameterValue);
                generator.writeStringField(ReportParam.TYPE, paramValue.getType().toString());
                boolean isProcessed = serializeAsList(paramValue, repartParameterValue, generator);
                if (!isProcessed) {
                    isProcessed = serializeAsMap(paramValue, repartParameterValue, generator);
                }
                if (!isProcessed) {
                    generator.writeStringField(ReportParam.VALUE, paramValue.getValue());
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }

    private boolean serializeAsList(ParamValue paramValue, Object repartParameterValue, JsonGenerator generator) throws IOException {
        boolean isList = paramValue.getType() == ReportParam.ParamTypes.List;
        if (isList) {
            generator.writeArrayFieldStart(ReportParam.VALUE);
            for (Object repartParameterItemValue : (List) repartParameterValue) {
                generator.writeStartObject();
                ParamValue paramItemValue = getParamValue(repartParameterItemValue);
                generator.writeStringField(ReportParam.TYPE, paramItemValue.getType().toString());
                boolean isProcessed = serializeAsList(paramItemValue, repartParameterItemValue, generator);
                if (!isProcessed) {
                    isProcessed = serializeAsMap(paramItemValue, repartParameterItemValue, generator);
                }
                if (!isProcessed) {
                    generator.writeStringField(ReportParam.VALUE, paramItemValue.getValue());
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        return isList;
    }

    private boolean serializeAsMap(ParamValue paramValue, Object repartParameterValue, JsonGenerator generator) throws IOException {
        boolean isMap = paramValue.getType() == ReportParam.ParamTypes.Map;
        if (isMap) {
            generator.writeArrayFieldStart(ReportParam.VALUE);
            Map valuesMap = (Map) repartParameterValue;
            for (Object key : valuesMap.keySet()) {
                generator.writeStartObject();
                Object repartParameterItemValue = valuesMap.get(key);
                ParamValue paramItemValue = getParamValue(repartParameterItemValue);
                generator.writeStringField(ReportParam.TYPE, paramItemValue.getType().toString());
                generator.writeStringField(ReportParam.MAPKEY, key.toString());
                boolean isProcessed = serializeAsList(paramItemValue, repartParameterItemValue, generator);
                if (!isProcessed) {
                    isProcessed = serializeAsMap(paramItemValue, repartParameterItemValue, generator);
                }
                if (!isProcessed) {
                    generator.writeStringField(ReportParam.VALUE, paramItemValue.getValue());
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        return isMap;
    }

    private ParamValue getParamValue(Object paramValue) {
        ParamValue result = new ParamValue();
        if (paramValue instanceof String){
            result.setType(ReportParam.ParamTypes.String);
            result.setValue((String) paramValue);
        }else if (paramValue instanceof Integer){
            result.setType(ReportParam.ParamTypes.Int);
            result.setValue(paramValue.toString());
        }else if (paramValue instanceof Long){
            result.setType(ReportParam.ParamTypes.Long);
            result.setValue(paramValue.toString());
        }else if (paramValue instanceof Double){
            result.setType(ReportParam.ParamTypes.Double);
            result.setValue(paramValue.toString());
        }else if (paramValue instanceof Boolean){
            result.setType(ReportParam.ParamTypes.Boolean);
            result.setValue(paramValue.toString());
        }else if (paramValue instanceof Calendar){
            result.setType(ReportParam.ParamTypes.Calendar);
            result.setValue(ReportParam.format.format(((Calendar)paramValue).getTime()));
        }else if (paramValue instanceof Date){
            result.setType(ReportParam.ParamTypes.DateTime);
            result.setValue(ReportParam.format.format((Date)paramValue));
        }else if (paramValue instanceof Id){
            result.setType(ReportParam.ParamTypes.Id);
            result.setValue(((Id)paramValue).toStringRepresentation());
        }else if (paramValue instanceof List){
            result.setType(ReportParam.ParamTypes.List);
            // Элементы листа формируются в вызывающем методе
        }else if (paramValue instanceof Map){
            result.setType(ReportParam.ParamTypes.Map);
            // Элементы карты формируются в вызывающем методе
        }else{
            // throw new UnsupportedOperationException("Unsupported parameter with type " + paramValue.getClass().getName());
            // По умолчанию String
            result.setType(ReportParam.ParamTypes.String);
            result.setValue(paramValue != null ? paramValue.toString() : "");
        }
        return result;
    }

}
