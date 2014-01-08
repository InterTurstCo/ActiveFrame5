package ru.intertrust.cm.core.gui.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploaderServlet;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService",
        urlPatterns = "/remote/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    private static  final String SESSION_ATTRIBUTE_UPLOAD_PROGRESS = "uploadIsCanceled";
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessUniverseServiceImpl.class);
    @EJB
    private GuiService guiService;

    @EJB
    CrudService crudServiceImpl;

    @EJB
    PersonManagementService personManagementService;

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization() {
        BusinessUniverseInitialization initialization = new BusinessUniverseInitialization();
        addInformationToInitializationObject(initialization);
        return initialization;
    }

    public BusinessUniverseInitialization addInformationToInitializationObject(BusinessUniverseInitialization businessUniverseInitialization){
        String currentLogin = guiService.getSessionContext().getCallerPrincipal().getName();
        Id personId = personManagementService.getPersonId(currentLogin);
        businessUniverseInitialization.setCurrentLogin(currentLogin);
        businessUniverseInitialization.setFirstName(crudServiceImpl.find(personId).getString("FirstName"));
        businessUniverseInitialization.setLastName(crudServiceImpl.find(personId).getString("LastName"));
        businessUniverseInitialization.seteMail(crudServiceImpl.find(personId).getString("EMail"));
        return businessUniverseInitialization;
    }

    @Override
    public Dto executeCommand(Command command) throws GuiException {
        try {
            return guiService.executeCommand(command);
        } catch (RuntimeException e) {

            throw handleEjbException(command, e);

        }
    }

    private GuiException handleEjbException(Command command, RuntimeException e) {
        if (e.getCause() instanceof GuiException) {
            return (GuiException) e.getCause();
        }
        return new GuiException("Command can't be executed: " + command.getName());
    }

    @Override
    public FormDisplayData getForm(Id domainObjectId) {
        return guiService.getForm(domainObjectId);
    }

    @Override
    public AttachmentUploadPercentage getAttachmentUploadPercentage() {

        HttpSession session = getThreadLocalRequest().getSession();
        AttachmentUploadPercentage uploadProgress = AttachmentUploaderServlet.getUploadProgress(session);

        return uploadProgress;
    }
}
