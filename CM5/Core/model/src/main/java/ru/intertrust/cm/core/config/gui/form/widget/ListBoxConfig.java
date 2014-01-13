package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 15/10/13
 *         Time: 17:28 PM
 */
@Root(name = "list-box")
public class ListBoxConfig extends WidgetConfig implements Dto {

    @Element(name = "pattern")
    private PatternConfig patternConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public SingleChoiceConfig getSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(SingleChoiceConfig singleChoice) {
        this.singleChoice = singleChoice;
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

        ListBoxConfig that = (ListBoxConfig) o;

        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null)  {
            return false;
        }

        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) : that.singleChoice != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
       int result =  super.hashCode();
       result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
       result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
       return result;
    }

    @Override
    public String getComponentName() {
        return "list-box";
    }

    @Override
    public boolean handlesMultipleObjects() {
        return true;
    }
}
