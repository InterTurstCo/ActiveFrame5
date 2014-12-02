package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;

import java.util.HashMap;

/**
 * Каждый FormProcessor - это prototype, поэтому непосредственно в объекте происходит кэширование
 * @author Denis Mitavskiy
 *         Date: 10.07.2014
 *         Time: 21:08
 */
public abstract class FormProcessor {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected PersonService personService;
    @Autowired
    protected AttachmentService attachmentService;

    private String userUid;

    private HashMap<String, WidgetHandler> widgetHandlers = new HashMap<>();

    public String getUserUid() {
        if (userUid == null) {
            userUid = personService.getCurrentPersonUid();
        }
        return userUid;
    }

    public WidgetHandler getWidgetHandler(WidgetConfig widgetConfig) {
        final String id = widgetConfig.getId();
        WidgetHandler widgetHandler = widgetHandlers.get(id);
        if (widgetHandler != null) {
            return widgetHandler;
        }
        widgetHandler = PluginHandlerHelper.getWidgetHandler(widgetConfig, applicationContext);
        widgetHandlers.put(id, widgetHandler);
        return widgetHandler;
    }
}
