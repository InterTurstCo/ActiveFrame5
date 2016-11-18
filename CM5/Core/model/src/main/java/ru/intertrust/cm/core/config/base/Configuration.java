package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.ConfigurationConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
@Convert(ConfigurationConverter.class)
@Namespace(reference = "https://cm5.intertrust.ru/config")
public class Configuration implements Serializable {

    public static String OPERATION_COLUMN = "operation";
    public static String COMPONENT_COLUMN = "component";
    public static String DOMAIN_OBJECT_ID_COLUMN = "domain_object_id";
    public static String IP_ADDRESS_COLUMN = "ip_address";
    public static String INFO_COLUMN = "info";
    public static String AUDIT_LOG_SUFFIX = "_al";
    public static String ID_COLUMN = "id";

    @ElementList(type = TopLevelConfig.class, inline = true)
    private List<TopLevelConfig> configurationList = new ArrayList<>();

    private String moduleName;

    public List<TopLevelConfig> getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List<TopLevelConfig> configurationList) {
        if (configurationList != null) {
            this.configurationList = configurationList;
        } else {
            this.configurationList.clear();
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().isAssignableFrom(o.getClass())) {
            return false;
        }

        Configuration that = (Configuration) o;

        if (configurationList != null ? !configurationList.equals(that.configurationList) : that.configurationList != null) {
            return false;
        }
        if (moduleName != null ? !moduleName.equals(that.moduleName) : that.moduleName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = configurationList != null ? configurationList.hashCode() : 0;
        result = 31 * result + (moduleName != null ? moduleName.hashCode() : 0);
        return result;
    }
}
