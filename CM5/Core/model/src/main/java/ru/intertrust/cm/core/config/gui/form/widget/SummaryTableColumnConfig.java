package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "summary-table-column")
public class SummaryTableColumnConfig extends ColumnParentConfig {
    @Element(name = "pattern", required = false)
    private PatternConfig patternConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
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

        SummaryTableColumnConfig that = (SummaryTableColumnConfig) o;

        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.
                formattingConfig != null) {
            return false;
        }
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        return result;
    }
}
