package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TableLayoutConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.08.2015
 *         Time: 21:17
 */
@Root(name = FormTableTemplateConfig.CONFIG_TAG_NAME)
public class FormTableTemplateConfig extends FormTemplateConfig {
    public static final String CONFIG_TAG_NAME = "form-table-template";

    @Element(name = "table", required = false)
    private TableLayoutConfig tableLayoutConfig;

    public TableLayoutConfig getTableLayoutConfig() {
        return tableLayoutConfig;
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

        FormTableTemplateConfig that = (FormTableTemplateConfig) o;

        if (tableLayoutConfig != null ? !tableLayoutConfig.equals(that.tableLayoutConfig) : that.tableLayoutConfig != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tableLayoutConfig != null ? tableLayoutConfig.hashCode() : 0);
        return result;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }
}
