package ru.intertrust.cm.core.dao.impl.doel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelFunction;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Функция DOEL, выполняющая форматирование числовых и временнЫх значений.
 * Обычно применяется на последнем шаге выражения для преобразования значений этих типов ({@link FieldType#DATETIME},
 * {@link FieldType#DATETIMEWITHTIMEZONE}, {@link FieldType#TIMELESSDATE}, {@link FieldType#LONG},
 * {@link FieldType#DECIMAL}) в строковые. К значениям других типов функция применяться не может.
 * Функция принимает 1 обязательный параметр, задающий шаблон форматирования даты или числа.
 * Правила форматирования соответствуют описанным в {@link NumberFormat} для чисел и {@link SimpleDateFormat} для дат.
 * <p><b>Пример использования:</b><br>
 * <code>Commission^parent.Job^parent.ExecutionTerm:format("dd-MMM-yy HH:mm:ss")</code>
 * 
 * @author apirozhkov
 */
@DoelFunction(name = "format",
        requiredParams = 1,
        contextTypes = {
                FieldType.DATETIME, FieldType.DATETIMEWITHTIMEZONE, FieldType.TIMELESSDATE,
                FieldType.DECIMAL, FieldType.LONG },
        changesType = true,
        resultType = FieldType.STRING)
public class FormatFunction implements DoelFunctionImplementation {

    private static final Logger logger = LoggerFactory.getLogger(FormatFunction.class);

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Value, S extends Value> List<T> process(List<? super S> context, String[] params,
            AccessToken accessToken) {
        String pattern = params[0];
        ArrayList<Value> result = new ArrayList<>(context.size());
        for (Object value : context) {
            String formatted = null;
            switch(((Value) value).getFieldType()) {
                case DATETIME:
                    formatted = formatDate(pattern, ((DateTimeValue) value).get());
                    break;
                case DATETIMEWITHTIMEZONE: {
                    DateTimeWithTimeZone dtz = ((DateTimeWithTimeZoneValue) value).get();
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeZone(TimeZone.getTimeZone(dtz.getTimeZoneContext().getTimeZoneId()));
                    cal.set(dtz.getYear(), dtz.getMonth(), dtz.getDayOfMonth(),
                            dtz.getHours(), dtz.getMinutes(), dtz.getSeconds());
                    formatted = formatDate(pattern, cal.getTime());
                    break;
                }
                case TIMELESSDATE: {
                    TimelessDate date = ((TimelessDateValue) value).get();
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.set(date.getYear(), date.getMonth(), date.getDayOfMonth());
                    formatted = formatDate(pattern, cal.getTime());
                    break;
                }
                case DECIMAL:
                    formatted = formatNumber(pattern, ((DecimalValue) value).get());
                    break;
                case LONG:
                    formatted = formatNumber(pattern, ((LongValue) value).get());
                    break;
                default:
                    logger.info("Can't format value of type " + ((Value) value).getFieldType());
                    continue;
            }
            result.add(new StringValue(formatted));
        }
        return (List<T>) result;
    }

    private String formatNumber(String pattern, Number number) {
        return new DecimalFormat(pattern).format(number);
    }

    private String formatDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }
}
