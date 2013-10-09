package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginData;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("some.active.plugin")
public class SomeActivePluginHandler extends PluginHandler {

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    GuiService guiService;

    public PluginData initialize(Dto param) {
        SomeActivePluginConfig config = (SomeActivePluginConfig) param;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        Form form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate) : guiService.getForm(config.getDomainObjectId());

        SomeActivePluginData pluginData = new SomeActivePluginData();
        pluginData.setForm(form);//getId("country", 1L)));
        return pluginData;
    }

    private Id getId(String doType, Long id) { // todo: rude hack. drop later
        Object obj = SpringApplicationContext.getContext().getBean("domainObjectTypeIdCache");
        try {
            Integer doTypeId = (Integer) obj.getClass().getMethod("getId", String.class).invoke(obj, doType);
            return new RdbmsId(doTypeId, id);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return super.hashCode();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
