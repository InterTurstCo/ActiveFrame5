package ru.intertrust.cm.test.acess.dynamicgroup;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.model.CollectorSettings;

@Root(name="test-dyn-group-collector-settings")
@Namespace(reference="https://cm5.intertrust.ru/testDynGroupSettings")
public class TestDynGroupCollectorSettings implements CollectorSettings{
    
    @Attribute(name="test-setting")
    private String testSetting;

    public String getTestSetting() {
        return testSetting;
    }

    public void setTestSetting(String testSetting) {
        this.testSetting = testSetting;
    }
}
