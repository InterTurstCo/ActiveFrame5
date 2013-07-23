package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.TopLevelConfig;

/**
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

    @Element(name="test-uniqueKey")
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

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
