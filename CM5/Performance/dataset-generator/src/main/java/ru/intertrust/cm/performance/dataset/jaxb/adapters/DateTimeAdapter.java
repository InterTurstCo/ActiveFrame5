
package ru.intertrust.cm.performance.dataset.jaxb.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.simpleframework.xml.transform.Transform;

/**
 * Адаптер преобразования значений для xml-типа dateTime
 *
 * @author Олег Еренцов
 */
public class DateTimeAdapter
    implements Transform<Date>
{

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    public Date read(String value) {
        Date date = null;
        try {
            date = format.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String write(Date value) {
        if (value == null) {
            return null;
        }
        return format.format(value);
    }

}
