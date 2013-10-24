package ru.intertrust.cm.core.config.model.gui.form.widget;

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

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
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
        return true;
    }

    @Override
    public int hashCode() {
       int result =  super.hashCode();
       result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
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
