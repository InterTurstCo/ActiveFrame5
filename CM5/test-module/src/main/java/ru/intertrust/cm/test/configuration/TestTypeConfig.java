package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Тестовая конфигурация верхнего уровня внешнего модуля
 * @author vmatsukevich
 *         Date: 7/17/13
 *         Time: 5:34 PM
 */
@Root(name = "test-type")
@Namespace(reference="https://cm5.intertrust.ru/custom-config")
public class TestTypeConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Element(name="test-fields")
    @Namespace(reference="https://cm5.intertrust.ru/custom-config")
    private TestFieldsConfig testFieldsConfig;

    @Element(name="test-uniqueKey", required = false)
    @Namespace(reference="https://cm5.intertrust.ru/custom-config")
    private TestUniqueKeyConfig testUniqueKeyConfig;

    public TestFieldsConfig getTestFieldsConfig() {
        return testFieldsConfig;
    }

    public void setTestFieldsConfig(TestFieldsConfig testFieldsConfig) {
        this.testFieldsConfig = testFieldsConfig;
    }

    public TestUniqueKeyConfig getTestUniqueKeyConfig() {
        return testUniqueKeyConfig;
    }

    public void setTestUniqueKeyConfig(TestUniqueKeyConfig testUniqueKeyConfig) {
        this.testUniqueKeyConfig = testUniqueKeyConfig;
    }

    @Override
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

        TestTypeConfig that = (TestTypeConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (testFieldsConfig != null ? !testFieldsConfig.equals(that.testFieldsConfig) : that.testFieldsConfig != null) {
            return false;
        }
        if (testUniqueKeyConfig != null ? !testUniqueKeyConfig.equals(that.testUniqueKeyConfig) : that.testUniqueKeyConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
