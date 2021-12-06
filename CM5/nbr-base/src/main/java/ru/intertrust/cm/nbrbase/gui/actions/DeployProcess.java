package ru.intertrust.cm.nbrbase.gui.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.impl.server.action.SimpleActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.model.FatalException;

@ComponentName("deploy.process.model")
public class DeployProcess extends SimpleActionHandler {

    protected static Logger log = LoggerFactory.getLogger(DeployProcess.class);

    @Autowired
    private ProcessService processService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {

        DomainObject processInfo = context.getMainFormState().getObjects().getRootDomainObject();

        processService.deployProcess(processInfo.getId());

        SimpleActionData actionData = new SimpleActionData();
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());
        actionData.setOnSuccessMessage("Установка выполнена");
        return actionData;
    }
}
