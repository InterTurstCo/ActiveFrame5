package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ComponentHelper;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Sergey.Okolot
 */
@ComponentName("favorite.toggle.action")
public class FavoriteToggleAction extends ToggleAction {
    private Element right;
    private Element center;

    @Override
    protected void execute() {
        ToggleActionContext actionContext = getInitialContext();
        actionContext.setPushed(!actionContext.isPushed());
        right = DOM.getElementById(ComponentHelper.RIGHT_ID);
        center = DOM.getElementById(ComponentHelper.DOMAIN_ID);

        if (actionContext.isPushed()) {
            openRightPanel();
        }
        if (!actionContext.isPushed()) {
            closeRightPanel();
        }
    }

    @Override
    public Component createNew() {
        return new FavoriteToggleAction();
    }

    private void openRightPanel() {
        if (center.hasClassName(CENTRAL_SECTION_FULL_SIZE_STYLE)) {
            center.setClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE);
            right.setClassName(RIGHT_SECTION_EXPANDED_FULL_STYLE);
        }else if(center.hasClassName(CENTRAL_SECTION_ACTIVE_STYLE)){
            center.setClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE);
            right.setClassName(RIGHT_SECTION_EXPANDED_STYLE);
        } else {
            center.setClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE);
            right.setClassName(RIGHT_SECTION_EXPANDED_STYLE);
        }
    }

    private void closeRightPanel() {
        if (center.hasClassName(CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE)) {
            center.setClassName(CENTRAL_SECTION_FULL_SIZE_STYLE);
            right.setClassName(RIGHT_SECTION_COLLAPSED_FULL_STYLE);
        } else if(center.hasClassName(CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE)){
            center.setClassName(CENTRAL_SECTION_ACTIVE_STYLE);
            right.setClassName(RIGHT_SECTION_COLLAPSED_STYLE);
        }
        else {
            center.setClassName(CENTRAL_SECTION_STYLE);
            right.setClassName(RIGHT_SECTION_COLLAPSED_STYLE);
        }
    }
}
