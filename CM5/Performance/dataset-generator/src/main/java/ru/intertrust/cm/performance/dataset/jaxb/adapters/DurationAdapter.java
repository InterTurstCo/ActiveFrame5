
package ru.intertrust.cm.performance.dataset.jaxb.adapters;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.simpleframework.xml.transform.Transform;

/**
 * Адаптер преобразования значений для xml-типа Duration
 *
 * @author Олег Еренцов
 */
public class DurationAdapter implements Transform<Duration>    
{


    public Duration read(String value) {
        Duration duration = null;
        try {
             duration = DatatypeFactory.newInstance().newDuration(value);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public String write(Duration value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

}
