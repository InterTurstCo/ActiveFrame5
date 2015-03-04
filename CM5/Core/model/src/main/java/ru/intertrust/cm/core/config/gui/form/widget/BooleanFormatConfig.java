package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.09.2014
 *         Time: 22:39
 */
@Root(name = "boolean-format")
public class BooleanFormatConfig extends AbstractTypeFormatConfig {
    @Attribute(name = "true-text")
    @Localizable
    private String trueText;

    @Attribute(name = "false-text")
    @Localizable
    private String falseText;

    @Attribute(name = "null-text")
    @Localizable
    private String nullText;

    public String getTrueText() {
        return trueText;
    }

    public void setTrueText(String trueText) {
        this.trueText = trueText;
    }

    public String getFalseText() {
        return falseText;
    }

    public void setFalseText(String falseText) {
        this.falseText = falseText;
    }

    public String getNullText() {
        return nullText;
    }

    public void setNullText(String nullText) {
        this.nullText = nullText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanFormatConfig)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BooleanFormatConfig that = (BooleanFormatConfig) o;

        if (falseText != null ? !falseText.equals(that.falseText) : that.falseText != null) {
            return false;
        }
        if (nullText != null ? !nullText.equals(that.nullText) : that.nullText != null) {
            return false;
        }
        if (trueText != null ? !trueText.equals(that.trueText) : that.trueText != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (trueText != null ? trueText.hashCode() : 0);
        result = 31 * result + (falseText != null ? falseText.hashCode() : 0);
        result = 31 * result + (nullText != null ? nullText.hashCode() : 0);
        return result;
    }
}
