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
}
