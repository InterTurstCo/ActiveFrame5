package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.CompactModeState;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ComponentHelper;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Sergey.Okolot
 */
@ComponentName("size.toggle.action")
public class SizeToggleAction extends ToggleAction {

    @Override
    protected void execute() {
        final CompactModeState compactModeState = Application.getInstance().getCompactModeState();
        final ToggleActionContext actionContext = getInitialContext();
        compactModeState.setExpanded(!compactModeState.isExpanded());
        actionContext.setPushed(compactModeState.isExpanded());
        updateSize(compactModeState);
    }

    private void updateSize(final CompactModeState state) {
        if (state.isExpanded()) {
            expand();
        } else {
            collapse();
        }
    }

    private void expand() {
        final Element left = DOM.getElementById(ComponentHelper.LEFT_ID);
        final Element header = DOM.getElementById(ComponentHelper.HEADER_ID);
        final Element center = DOM.getElementById(ComponentHelper.DOMAIN_ID);
        center.getStyle().clearMarginLeft();
        final Element right = DOM.getElementById(ComponentHelper.RIGHT_ID);
        if (center.hasClassName(CENTRAL_SECTION_STYLE)) {
            center.replaceClassName(CENTRAL_SECTION_STYLE, CENTRAL_SECTION_FULL_SIZE_STYLE);

        } else if (center.hasClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE)) {
            center.replaceClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE, CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE);
            right.setClassName(RIGHT_SECTION_EXPANDED_FULL_STYLE);
        } else if (center.hasClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE)) {
            center.replaceClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE, CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE);
            right.setClassName(RIGHT_SECTION_EXPANDED_FULL_STYLE);
        } else {
            center.replaceClassName(CENTRAL_SECTION_ACTIVE_STYLE, CENTRAL_SECTION_FULL_SIZE_STYLE);
        }
        left.replaceClassName(LEFT_SECTION_STYLE, LEFT_SECTION_COLLAPSED_STYLE);
        header.replaceClassName(TOP_SECTION_STYLE, TOP_SECTION_COLLAPSED_STYLE);

    }

    private void collapse() {
        final Element left = DOM.getElementById(ComponentHelper.LEFT_ID);
        final Element header = DOM.getElementById(ComponentHelper.HEADER_ID);
        final Element center = DOM.getElementById(ComponentHelper.DOMAIN_ID);

        if (center.hasClassName(CENTRAL_SECTION_FULL_SIZE_STYLE)) {
            center.replaceClassName(CENTRAL_SECTION_FULL_SIZE_STYLE, CENTRAL_SECTION_STYLE);
            if (Application.getInstance().getCompactModeState().isNavigationTreePanelExpanded()) {
                center.addClassName(CENTRAL_SECTION_ACTIVE_STYLE);
                center.getStyle().setMarginLeft(Application.getInstance().getCompactModeState().getFullNavigationPanelWidth(),
                        Style.Unit.PX);
                center.removeClassName(CENTRAL_SECTION_STYLE);
            }
        } else {
            if (Application.getInstance().getCompactModeState().isNavigationTreePanelExpanded()) {
                center.replaceClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE, CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE);
                center.addClassName(CENTRAL_SECTION_ACTIVE_STYLE);
            } else {
                center.replaceClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE, CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE);
            }

        }
        left.replaceClassName(LEFT_SECTION_COLLAPSED_STYLE, LEFT_SECTION_STYLE);
        header.replaceClassName(TOP_SECTION_COLLAPSED_STYLE, TOP_SECTION_STYLE);

    }

    @Override
    public Component createNew() {
        return new SizeToggleAction();
    }
}
