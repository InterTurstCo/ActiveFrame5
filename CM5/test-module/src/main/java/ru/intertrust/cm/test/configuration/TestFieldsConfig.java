package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/17/13
 *         Time: 5:36 PM
 */
@Namespace(reference="https://cm5.intertrust.ru/custom-config")
public class TestFieldsConfig {

    @ElementList(entry = "test-field", inline = true)
    private List<TestFieldConfig> testFieldConfigs;

    public List<TestFieldConfig> getTestFieldConfigs() {
        return testFieldConfigs;
    }

    public void setTestFieldConfigs(List<TestFieldConfig> testFieldConfigs) {
        this.testFieldConfigs = testFieldConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestFieldsConfig that = (TestFieldsConfig) o;

        if (testFieldConfigs != null ? !testFieldConfigs.equals(that.testFieldConfigs) : that.testFieldConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return testFieldConfigs != null ? testFieldConfigs.hashCode() : 0;
    }
}
