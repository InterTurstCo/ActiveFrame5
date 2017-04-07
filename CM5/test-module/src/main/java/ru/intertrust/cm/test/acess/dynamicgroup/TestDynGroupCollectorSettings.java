package ru.intertrust.cm.test.acess.dynamicgroup;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.CollectorSettings;

@Root(name="test-dyn-group-collector-settings")
public class TestDynGroupCollectorSettings implements CollectorSettings{

    @Attribute(name="test-setting")
    private String testSetting;

    public String getTestSetting() {
        return testSetting;
    }

    public void setTestSetting(String testSetting) {
        this.testSetting = testSetting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestDynGroupCollectorSettings that = (TestDynGroupCollectorSettings) o;

        if (testSetting != null ? !testSetting.equals(that.testSetting) : that.testSetting != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return testSetting != null ? testSetting.hashCode() : 0;
    }
}
