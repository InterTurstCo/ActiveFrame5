package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "number-format")
public class NumberFormatConfig extends AbstractTypeFormatConfig {
    @Attribute(name = "pattern")
    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberFormatConfig)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NumberFormatConfig that = (NumberFormatConfig) o;

        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        return result;
    }
}
