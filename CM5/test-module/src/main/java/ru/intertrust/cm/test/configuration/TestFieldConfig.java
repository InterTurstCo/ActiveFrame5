package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Namespace;

/**
 * @author vmatsukevich
 *         Date: 7/17/13
 *         Time: 5:36 PM
 */
@Namespace(reference="https://cm5.intertrust.ru/custom-config")
public class TestFieldConfig {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
