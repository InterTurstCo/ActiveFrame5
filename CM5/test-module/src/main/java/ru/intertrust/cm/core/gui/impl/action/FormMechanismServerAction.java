package ru.intertrust.cm.core.gui.impl.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * @author Denis Mitavskiy
 *         Date: 03.05.2017
 *         Time: 14:59
 */
@ComponentName("form.mechanism.server.action")
public class FormMechanismServerAction extends ActionHandler<SimpleActionContext, SimpleActionData> {
    private static final Logger logger = LoggerFactory.getLogger(FormMechanismServerAction.class);

    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private ConfigurationControlService configurationControlService;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        logger.info("Start plugin TestPlugin1");
        final SimpleActionData result = new SimpleActionData();
        return result;
    }
}
