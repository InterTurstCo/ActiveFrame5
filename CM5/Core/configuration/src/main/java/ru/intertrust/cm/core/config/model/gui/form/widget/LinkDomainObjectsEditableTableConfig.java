package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-domain-objects-editable-table")
public class LinkDomainObjectsEditableTableConfig extends WidgetConfig {

    @Element(name = "linked-form")
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "form-table")
    private FormTableConfig formTableConfig;

    public LinkedFormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(LinkedFormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
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
        LinkDomainObjectsEditableTableConfig that = (LinkDomainObjectsEditableTableConfig) o;

        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null) {
            return false;
        }
        if (formTableConfig != null ? !formTableConfig.equals(that.formTableConfig) : that.
                formTableConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (formTableConfig != null ? formTableConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}



