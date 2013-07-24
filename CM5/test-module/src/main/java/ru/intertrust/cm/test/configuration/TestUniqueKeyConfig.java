package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/17/13
 *         Time: 5:37 PM
 */
@Namespace(reference="https://cm5.intertrust.ru/custom-config")
public class TestUniqueKeyConfig {

    @ElementList(entry="field", inline = true)
    private List<TestUniqueKeyFieldConfig> testUniqueKeyFieldConfigs;

    public List<TestUniqueKeyFieldConfig> getTestUniqueKeyFieldConfigs() {
        return testUniqueKeyFieldConfigs;
    }

    public void setTestUniqueKeyFieldConfigs(List<TestUniqueKeyFieldConfig> testUniqueKeyFieldConfigs) {
        this.testUniqueKeyFieldConfigs = testUniqueKeyFieldConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestUniqueKeyConfig that = (TestUniqueKeyConfig) o;

        if (testUniqueKeyFieldConfigs != null ? !testUniqueKeyFieldConfigs.equals(that.testUniqueKeyFieldConfigs) : that.testUniqueKeyFieldConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return testUniqueKeyFieldConfigs != null ? testUniqueKeyFieldConfigs.hashCode() : 0;
    }
}
