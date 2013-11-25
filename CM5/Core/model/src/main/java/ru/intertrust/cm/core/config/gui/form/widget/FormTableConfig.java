package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "form-table")
public class FormTableConfig implements Dto {
    @Attribute(name = "form-name")
    private String formName;
    @ElementList(inline = true)
    private List<FormTableColumnConfig> formTableColumnConfigList = new ArrayList<FormTableColumnConfig>();

    public List<FormTableColumnConfig> getFormTableColumnConfig() {
        return formTableColumnConfigList ;
    }

    public void setFormTableColumnConfig(List<FormTableColumnConfig> formTableColumnConfigList) {
        this.formTableColumnConfigList  = formTableColumnConfigList ;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormTableConfig that = (FormTableConfig) o;

        if (formTableColumnConfigList  != null ? !formTableColumnConfigList .equals(that.
                formTableColumnConfigList ) : that.formTableColumnConfigList  != null) {
            return false;
        }
        if (formName != null ? !formName.equals(that.formName) : that.formName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = formName != null ? formName.hashCode() : 0;
        result = 23 * result + (formTableColumnConfigList  != null ? formTableColumnConfigList .hashCode() : 0);
        return result;
    }
}
