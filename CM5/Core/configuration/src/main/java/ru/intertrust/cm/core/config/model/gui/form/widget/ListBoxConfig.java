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
       return super.equals(o);
    }

    @Override
    public int hashCode() {
       return  super.hashCode();
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
