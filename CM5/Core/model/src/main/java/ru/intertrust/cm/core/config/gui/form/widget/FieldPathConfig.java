package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.FieldPathOnDeleteActionConverter;
import ru.intertrust.cm.core.config.gui.form.DefaultValueConfig;
import ru.intertrust.cm.core.config.gui.form.DefaultValuesConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "field-path")
@Convert(FieldPathOnDeleteActionConverter.class)
public class FieldPathConfig implements Dto {
    public static final String CASCADE_STRING = "cascade";
    public static final String UNLINK_STRING = "unlink";

    public static enum OnDeleteAction {
        CASCADE(CASCADE_STRING),
        UNLINK(UNLINK_STRING);

        private final String string;

        private OnDeleteAction() {
            string = null;
        }

        private OnDeleteAction(String str) {
            this.string = str;
        }

        public String getString() {
            return string;
        }

        public static OnDeleteAction getEnum(String name) {
            switch (name) {
                case CASCADE_STRING:
                    return CASCADE;
                case UNLINK_STRING:
                    return UNLINK;
            }
            return null;
        }
    }

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name = "domain-object-linker", required = false)
    private String domainObjectLinker;

    @Attribute(name="on-root-delete", required = false)
    private OnDeleteAction onRootDelete;

    @Element(name = "on-link", required = false)
    private OnLinkConfig onLinkConfig;

    @Element(name = "on-unlink", required = false)
    private OnUnlinkConfig onUnlinkConfig;

    @Element(name = "default-value", required = false)
    private DefaultValueConfig defaultValueConfig;

    @Element(name = "default-values", required = false)
    private DefaultValuesConfig defaultValuesConfig;

    @Element(name = "exact-types", required = false)
    private ExactTypesConfig exactTypesConfig;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OnDeleteAction getOnRootDelete() {
        return onRootDelete;
    }

    public void setOnRootDelete(OnDeleteAction onRootDelete) {
        this.onRootDelete = onRootDelete;
    }

    public String getDomainObjectLinker() {
        return domainObjectLinker;
    }

    public void setDomainObjectLinker(String domainObjectLinker) {
        this.domainObjectLinker = domainObjectLinker;
    }

    public OnUnlinkConfig getOnUnlinkConfig() {
        return onUnlinkConfig;
    }

    public void setOnUnlinkConfig(OnUnlinkConfig onUnlinkConfig) {
        this.onUnlinkConfig = onUnlinkConfig;
    }

    public OnLinkConfig getOnLinkConfig() {
        return onLinkConfig;
    }

    public void setOnLinkConfig(OnLinkConfig onLinkConfig) {
        this.onLinkConfig = onLinkConfig;
    }

    public DefaultValueConfig getDefaultValueConfig() {
        return defaultValueConfig;
    }

    public void setDefaultValueConfig(DefaultValueConfig defaultValueConfig) {
        this.defaultValueConfig = defaultValueConfig;
    }

    public DefaultValuesConfig getDefaultValuesConfig() {
        return defaultValuesConfig;
    }

    public void setDefaultValuesConfig(DefaultValuesConfig defaultValuesConfig) {
        this.defaultValuesConfig = defaultValuesConfig;
    }

    public ExactTypesConfig getExactTypesConfig() {
        return exactTypesConfig;
    }

    public void setExactTypesConfig(ExactTypesConfig exactTypesConfig) {
        this.exactTypesConfig = exactTypesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldPathConfig that = (FieldPathConfig) o;

        if (domainObjectLinker != null ? !domainObjectLinker.equals(that.domainObjectLinker) : that.domainObjectLinker != null) {
            return false;
        }
        if (onLinkConfig != null ? !onLinkConfig.equals(that.onLinkConfig) : that.onLinkConfig != null) {
            return false;
        }
        if (onRootDelete != that.onRootDelete) {
            return false;
        }
        if (onUnlinkConfig != null ? !onUnlinkConfig.equals(that.onUnlinkConfig) : that.onUnlinkConfig != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (exactTypesConfig != null ? !exactTypesConfig.equals(that.exactTypesConfig) : that.exactTypesConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
