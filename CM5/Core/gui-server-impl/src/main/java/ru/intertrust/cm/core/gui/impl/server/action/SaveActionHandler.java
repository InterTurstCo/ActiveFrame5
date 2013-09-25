package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.SomeActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:18
 */
@ComponentName("save.action")
public class SaveActionHandler extends ActionHandler {
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {
        DomainObject rootDomainObject = getGuiService().saveForm(((SaveActionContext) context).getForm());
        SomeActivePluginHandler handler = (SomeActivePluginHandler) applicationContext.getBean("some.active.plugin");
        SomeActivePluginConfig config = new SomeActivePluginConfig(rootDomainObject.getId());
        SaveActionData result = new SaveActionData();
        result.setSomeActivePluginData((SomeActivePluginData) handler.initialize(config));
        return (T) result;
    }

    private GuiService getGuiService() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            return (GuiService) ctx.lookup("java:app/web-app/GuiServiceImpl!ru.intertrust.cm.core.gui.api.server.GuiService");
        } catch (NamingException ex) {
            throw new GuiException("EJB not found", ex);
        }
    }
}
