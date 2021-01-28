package ru.intertrust.cm.core.rest.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
                Object repartParaneterValue = value.getParams().get(parameterName);
                ParamValue paramValue = getParamValue(repartParaneterValue);
                generator.writeStringField(ReportParam.TYPE, paramValue.getType().toString());
                if (paramValue.getType() == ReportParam.ParamTypes.List){
                    generator.writeArrayFieldStart(ReportParam.VALUE);
                    for (Object item : (List)repartParaneterValue) {
                        generator.writeStartObject();
                        ParamValue paramItemValue = getParamValue(item);
                        generator.writeStringField(ReportParam.TYPE, paramItemValue.getType().toString());
                        generator.writeStringField(ReportParam.VALUE, paramItemValue.getValue());
                        generator.writeEndObject();
                    }
                    generator.writeEndArray();
                }else {
                    generator.writeStringField(ReportParam.VALUE, paramValue.getValue());
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
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
        }else if (paramValue instanceof Date){
            result.setType(ReportParam.ParamTypes.DateTime);
            result.setValue(ReportParam.format.format((Date)paramValue));
        }else if (paramValue instanceof Id){
            result.setType(ReportParam.ParamTypes.Id);
            result.setValue(((Id)paramValue).toStringRepresentation());
        }else if (paramValue instanceof List){
            result.setType(ReportParam.ParamTypes.List);
            // Элементы листа формируются в вызывающем методе
        }else{
            throw new UnsupportedOperationException("Unsupported parameter with type " + paramValue.getClass().getName());
        }
        return result;
    }

}
