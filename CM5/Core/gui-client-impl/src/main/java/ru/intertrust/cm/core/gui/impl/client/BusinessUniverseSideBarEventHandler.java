package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.CompactModeState;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.04.2015
 *         Time: 12:02
 */
public class BusinessUniverseSideBarEventHandler implements SideBarResizeEventHandler {
    @Override
    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
        final CompactModeState compactModeState = Application.getInstance().getCompactModeState();
        Element left = DOM.getElementById(ComponentHelper.LEFT_ID);
        if (!compactModeState.isExpanded()) {
            left.setClassName(event.getStyleForLeftSector());
            setCentralDivPanelClassName(event.getStyleForCenterSector());
        }
    }
    private void setCentralDivPanelClassName(String newCenterClassName){
        if (CENTRAL_SECTION_ACTIVE_STYLE.equalsIgnoreCase(newCenterClassName)) {
            setClassNameForPinnedLeftPanel(newCenterClassName);
        }else {
            setClassNameForNotPinnedLeftPanel(newCenterClassName);
        }
    }
    private void setClassNameForPinnedLeftPanel(String newCenterClassName){
        Element center = DOM.getElementById(ComponentHelper.DOMAIN_ID);
        if (center.hasClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE)) {
            center.setClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE);
        } else {
            center.setClassName(newCenterClassName);
        }
    }
    private void setClassNameForNotPinnedLeftPanel(String newCenterClassName){
        Element center = DOM.getElementById(ComponentHelper.DOMAIN_ID);
        if(center.hasClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE)){
            center.setClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE);
        } else if(!center.hasClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE)){
            center.setClassName(newCenterClassName);
        }

    }
}
