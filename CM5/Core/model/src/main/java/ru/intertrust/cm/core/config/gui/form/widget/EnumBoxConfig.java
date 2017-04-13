package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 18:50
 */
@Root(name = "enumeration-box")
public class EnumBoxConfig extends WidgetConfig {

    @Element(name="mapping", required = false)
    private EnumMappingConfig enumMappingConfig;

    @Element(name="map-provider", required = false)
    private EnumMapProviderConfig enumMapProviderConfig;

    @Override
    public String getComponentName() {
        return "enumeration-box";
    }

    @Override
    public String getLogicalValidatorComponentName() {
        return "enumBoxLogicalValidator";
    }

    public EnumMappingConfig getEnumMappingConfig() {
        return enumMappingConfig;
    }

    public EnumMapProviderConfig getEnumMapProviderConfig() {
        return enumMapProviderConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        EnumBoxConfig that = (EnumBoxConfig) o;
        if (enumMapProviderConfig != null ? !enumMapProviderConfig.equals(that.enumMapProviderConfig) : that
                .enumMapProviderConfig != null) {
            return false;
        }
        if (enumMappingConfig != null ? !enumMappingConfig.equals(that.enumMappingConfig) : that.enumMappingConfig !=
                null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enumMappingConfig != null ? enumMappingConfig.hashCode() : 0);
        result = 31 * result + (enumMapProviderConfig != null ? enumMapProviderConfig.hashCode() : 0);
        return result;
    }
}
