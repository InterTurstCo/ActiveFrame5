
package ru.intertrust.cm.performance.dataset.jaxb.adapters;

import java.util.Date;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

import org.simpleframework.xml.transform.Transform;

/**
 * Адаптер преобразования значений для xml-типа dateTime
 *
 * @author Олег Еренцов
 */
public class DateTimeAdapter
    implements Transform<Date>
{

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public Date read(String value) {
        Date date = null;
        try {
            date = ThreadSafeDateFormat.parse(value, DATE_TIME_PATTERN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public String write(Date value) {
        if (value == null) {
            return null;
        }
        return ThreadSafeDateFormat.format(value, DATE_TIME_PATTERN);
    }

}
