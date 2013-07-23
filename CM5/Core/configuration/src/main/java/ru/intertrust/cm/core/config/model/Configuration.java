package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.ConfigurationConverter;

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
public class Configuration implements Serializable {
    @ElementList(type=TopLevelConfig.class, inline=true)
    private List<TopLevelConfig> configurationList = new ArrayList<>();

    public List<TopLevelConfig> getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List<TopLevelConfig> configurationList) {
        if(configurationList != null) {
            this.configurationList = configurationList;
        } else {
            this.configurationList.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Configuration that = (Configuration) o;

        if (configurationList != null ? !configurationList.equals(that.configurationList) : that.configurationList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return configurationList != null ? configurationList.hashCode() : 0;
    }
}
