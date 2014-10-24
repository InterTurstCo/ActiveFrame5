package ru.intertrust.cm.core.config.gui.form.widget.linkediting;

import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;

import java.util.List;

/**
 * Created by andrey on 21.10.14.
 */
public class LinkedFormViewerConfig extends FormViewerConfig {

    private List<LinkedFormConfig> linkedFormConfig;

    public List<LinkedFormConfig> getLinkedFormConfigs() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(List<LinkedFormConfig> linkedFormConfigs) {
        this.linkedFormConfig = linkedFormConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LinkedFormViewerConfig that = (LinkedFormViewerConfig) o;

        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        return result;
    }
}
