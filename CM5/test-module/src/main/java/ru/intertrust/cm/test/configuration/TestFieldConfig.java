package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Namespace;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author vmatsukevich
 *         Date: 7/17/13
 *         Time: 5:36 PM
 */
@Namespace(reference="https://cm5.intertrust.ru/custom-config")
public class TestFieldConfig implements Dto {

    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestFieldConfig that = (TestFieldConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
