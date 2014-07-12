package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 18.06.2014.
 */
@Root(name = "on-link")
public class OnLinkConfig implements Dto {

    @Attribute(name = "do-link", required = false)
    private boolean doLink = true;

    @Element(name= "create")
    private CreateConfig createConfig;

    @Element(name= "update")
    private UpdateConfig updateConfig;

    public CreateConfig getCreateConfig() {
        return createConfig;
    }

    public void setCreateConfig(CreateConfig createConfig) {
        this.createConfig = createConfig;
    }

    public UpdateConfig getUpdateConfig() {
        return updateConfig;
    }

    public void setUpdateConfig(UpdateConfig updateConfig) {
        this.updateConfig = updateConfig;
    }

    public boolean doLink() {
        return doLink;
    }

    public void setDoLink(boolean doLink) {
        this.doLink = doLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnLinkConfig that = (OnLinkConfig) o;

        if (doLink != that.doLink) return false;
        if (createConfig != null ? !createConfig.equals(that.createConfig) : that.createConfig != null) return false;
        if (updateConfig != null ? !updateConfig.equals(that.updateConfig) : that.updateConfig != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (doLink ? 1 : 0);
        result = 31 * result + (createConfig != null ? createConfig.hashCode() : 0);
        result = 31 * result + (updateConfig != null ? updateConfig.hashCode() : 0);
        return result;
    }
}
