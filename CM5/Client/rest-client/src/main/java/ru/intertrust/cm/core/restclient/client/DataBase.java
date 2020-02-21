package ru.intertrust.cm.core.restclient.client;

import ru.intertrust.cm.core.restclient.model.FieldData;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class DataBase {

    protected DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
    protected DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    protected DateTimeFormatter dateTimeWithTimeZoneFormat =  DateTimeFormatter.ISO_ZONED_DATE_TIME;

    protected Object getFieldValue(FieldData restField) throws ParseException {
        Object result = null;
        switch (restField.getType()){
            case LONG:
                result = Long.parseLong(restField.getValue());
                break;
            case BOOLEAN:
                result = Boolean.parseBoolean(restField.getValue());
                break;
            case DECIMAL:
                result = Double.parseDouble(restField.getValue());
                break;
            case REFERENCE:
                result = restField.getValue();
                break;
            case STRING:
                result = restField.getValue();
                break;
            case TEXT:
                result = restField.getValue();
                break;
            case DATETIME:
                result = LocalDateTime.parse(restField.getValue(), dateTimeFormat);
                break;
            case DATETIMEWITHTIMEZONE:
                result = ZonedDateTime.parse(restField.getValue(), dateTimeWithTimeZoneFormat);
                break;
            case TIMELESSDATE:
                result = LocalDate.parse(restField.getValue(), dateFormat);
                break;
        }
        return result;
    }

}
