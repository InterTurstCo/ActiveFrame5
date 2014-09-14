package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.09.2014
 *         Time: 0:31
 */
public abstract class AbstractTypeFormatConfig implements Dto {

    @Element(name = "field-paths")
    private FieldPathsConfig fieldsPathConfig;

    public FieldPathsConfig getFieldsPathConfig() {
        return fieldsPathConfig;
    }

    public void setFieldsPathConfig(FieldPathsConfig fieldsPathConfig) {
        this.fieldsPathConfig = fieldsPathConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractTypeFormatConfig that = (AbstractTypeFormatConfig) o;

        if (fieldsPathConfig != null ? !fieldsPathConfig.equals(that.fieldsPathConfig) : that.fieldsPathConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fieldsPathConfig != null ? fieldsPathConfig.hashCode() : 0;
    }
}
