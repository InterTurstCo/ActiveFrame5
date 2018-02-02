package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomePluginData;

/**
 * Created by Ravil on 01.02.2018.
 */
@ComponentName("ServerService.plugin")
public class ServerServiceHandler extends PluginHandler {

    @Autowired
    PersonService personService;

    private static final Logger logger = LoggerFactory.getLogger(ServerServiceHandler.class);

    public PluginData writeLog(Dto object) {
        SomePluginData dto = (SomePluginData) object;
        logger.info(personService.getCurrentPersonUid()+" " +dto.getText());
        return new SomePluginData();
    }
}
