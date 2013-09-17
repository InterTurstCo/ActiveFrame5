
package ru.intertrust.cm.performance.dataset.jaxb.adapters;


import org.simpleframework.xml.transform.Transform;

/**
 * Адаптер преобразования значений для xml-типа NonNegativeInteger
 *
 * @author Олег Еренцов
 */
public class NonNegativeIntegerAdapter
    implements Transform<Integer>
{


    public Integer read(String value) {
        return new Integer(value);
    }

    public String write(Integer value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

}
