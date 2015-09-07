package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.2015
 *         Time: 8:44
 */
public class InitialNavigationLinkActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "initial.navigation.link.action";
    private String initialNavigationLink;

    public InitialNavigationLinkActionContext() {
    }

    public InitialNavigationLinkActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getInitialNavigationLink() {
        return initialNavigationLink;
    }

    public void setInitialNavigationLink(String initialNavigationLink) {
        this.initialNavigationLink = initialNavigationLink;
    }
}
