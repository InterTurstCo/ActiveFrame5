package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ComponentHelper;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;

/**
 * @author Sergey.Okolot
 */
@ComponentName("favorite.toggle.action")
public class FavoriteToggleAction extends ToggleAction {
    private Element right;
    private Element center;
    private Element footer;

    @Override
    protected void execute() {
        ToggleActionContext actionContext = getInitialContext();
        actionContext.setPushed(!actionContext.isPushed());
        right = DOM.getElementById(ComponentHelper.RIGHT_ID);
        center = DOM.getElementById(ComponentHelper.DOMAIN_ID);

        if(actionContext.isPushed()){
            openRightPanel();;
        }
        if(!actionContext.isPushed()){
            closeRightPanel();
        }
    }

    @Override
    public Component createNew() {
        return new FavoriteToggleAction();
    }

    private void openRightPanel(){
        right.setClassName("stickerPanelOn");
        center.setClassName("centralPanelRightPanelOpen");
    }

    private void closeRightPanel(){
        right.setClassName("stickerPanelOff");
        center.setClassName("central-div-panel-test");
    }
}
