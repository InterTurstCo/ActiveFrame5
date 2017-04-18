package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.08.2015
 *         Time: 21:11
 */
@Root(name = FormTabTemplateConfig.CONFIG_TAG_NAME)
public class FormTabTemplateConfig extends FormTemplateConfig {
    public static final String CONFIG_TAG_NAME = "form-tab-template";

    @Element(name = "tab", required = false)
    private TabConfig tabConfig;

    public TabConfig getTabConfig() {
        return tabConfig;
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

        FormTabTemplateConfig that = (FormTabTemplateConfig) o;

        if (tabConfig != null ? !tabConfig.equals(that.tabConfig) : that.tabConfig != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tabConfig != null ? tabConfig.hashCode() : 0);
        return result;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }
}
