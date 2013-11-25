package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-domain-objects-table")
public class LinkedDomainObjectsTableConfig extends WidgetConfig {

    @Element(name = "linked-form")
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "pattern", required = false)
    private PatternConfig patternConfig;

    @Element(name = "summary-table")
    private SummaryTableConfig summaryTableConfig;

    public LinkedFormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(LinkedFormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
    }

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public SummaryTableConfig getSummaryTableConfig() {
        return summaryTableConfig;
    }

    public void setSummaryTableConfig(SummaryTableConfig summaryTableConfig) {
        this.summaryTableConfig = summaryTableConfig;
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

        LinkedDomainObjectsTableConfig that = (LinkedDomainObjectsTableConfig) o;

        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null) {
            return false;
        }
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null) {
            return false;
        }
        if (summaryTableConfig != null ? !summaryTableConfig.equals(that.patternConfig) : that.
                summaryTableConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
        result = 31 * result + (summaryTableConfig != null ? summaryTableConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}

