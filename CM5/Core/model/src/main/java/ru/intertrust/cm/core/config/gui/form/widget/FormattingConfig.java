package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "formatting")
public class FormattingConfig implements Dto {
    @Element(name = "date-format", required = false)
    private DateFormatConfig dateFormatConfig;

    @Element(name = "number-format", required = false)
    private NumberFormatConfig numberFormatConfig;

    public DateFormatConfig getDateFormatConfig() {
        return dateFormatConfig;
    }

    public void setDateFormatConfig(DateFormatConfig dateFormatConfig) {
        this.dateFormatConfig = dateFormatConfig;
    }

    public NumberFormatConfig getNumberFormatConfig() {
        return numberFormatConfig;
    }

    public void setNumberFormatConfig(NumberFormatConfig numberFormatConfig) {
        this.numberFormatConfig = numberFormatConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormattingConfig that = (FormattingConfig) o;

        if (dateFormatConfig != null ? !dateFormatConfig.equals(that.dateFormatConfig) : that.dateFormatConfig != null) {
            return false;
        }
        if (numberFormatConfig != null ? !numberFormatConfig.equals(that.numberFormatConfig) : that.
                numberFormatConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = dateFormatConfig != null ? dateFormatConfig.hashCode() : 0;
        result = 31 * result + (numberFormatConfig != null ? numberFormatConfig.hashCode() : 0);
        return result;
    }
}
