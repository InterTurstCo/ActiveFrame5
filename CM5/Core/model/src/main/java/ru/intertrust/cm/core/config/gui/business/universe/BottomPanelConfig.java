package ru.intertrust.cm.core.config.gui.business.universe;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.04.2015
 *         Time: 19:22
 */
@Root(name = "bottom-panel")
public class BottomPanelConfig implements Dto {
    @Element(name = "stick-notes", required = true)
    private StickNotesConfig stickNotesConfig;

    public StickNotesConfig getStickNotesConfig() {
        return stickNotesConfig;
    }

    public void setStickNotesConfig(StickNotesConfig stickNotesConfig) {
        this.stickNotesConfig = stickNotesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BottomPanelConfig that = (BottomPanelConfig) o;

        if (stickNotesConfig != null ? !stickNotesConfig.equals(that.stickNotesConfig) : that.stickNotesConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return stickNotesConfig != null ? stickNotesConfig.hashCode() : 0;
    }
}
