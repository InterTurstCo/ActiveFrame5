package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.gui.form.widget.UniqueKeyValueConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.11.2014
 *         Time: 17:34
 */
public abstract class ComplicatedParamConfig extends ParamConfig {

    @Element(name = "unique-key-value", required = false)
    private UniqueKeyValueConfig uniqueKeyValueConfig;

    public UniqueKeyValueConfig getUniqueKeyValueConfig() {
        return uniqueKeyValueConfig;
    }

    public void setUniqueKeyValueConfig(UniqueKeyValueConfig uniqueKeyValueConfig) {
        this.uniqueKeyValueConfig = uniqueKeyValueConfig;
    }

    public abstract String getWidgetId();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) return false;

        ComplicatedParamConfig that = (ComplicatedParamConfig) o;

        if (uniqueKeyValueConfig != null ? !uniqueKeyValueConfig.equals(that.uniqueKeyValueConfig)
                : that.uniqueKeyValueConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uniqueKeyValueConfig != null ? uniqueKeyValueConfig.hashCode() : 0);
        return result;
    }
}
