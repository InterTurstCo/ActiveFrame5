package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 02.09.14
 *         Time: 14:45
 */
@Root(name = "form-widget-access")
public class FormWidgetAccessConfig implements Dto {

    @Attribute(name = "form")
    private String form;

    @ElementList(inline = true)
    private List<HideWidgetConfig> hideWidgetsConfigList = new ArrayList<HideWidgetConfig>();

    public String getForm() {
        return form;
    }

    public List<HideWidgetConfig> getHideWidgetsConfigList() {
        return hideWidgetsConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormWidgetAccessConfig that = (FormWidgetAccessConfig) o;

        if (form != null ? !form.equals(that.form) : that.form != null) {
            return false;
        }
        if (hideWidgetsConfigList != null ? !hideWidgetsConfigList.equals(that.hideWidgetsConfigList) : that
                .hideWidgetsConfigList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = form != null ? form.hashCode() : 0;
        result = 31 * result + (hideWidgetsConfigList != null ? hideWidgetsConfigList.hashCode() : 0);
        return result;
    }
}
