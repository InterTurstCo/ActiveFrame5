package ru.intertrust.cm.core.gui.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.LogoConfig;
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
    private static final String DEFAULT_LOGO_PATH = "logo.gif";
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessUniverseServiceImpl.class);
    @EJB
    private GuiService guiService;

    @EJB
    CrudService crudServiceImpl;

    @EJB
    PersonService personService;

    @EJB
    private ConfigurationService configurationService;

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization() {
        BusinessUniverseInitialization initialization = new BusinessUniverseInitialization();
        addInformationToInitializationObject(initialization);
        addLogoImagePath(initialization);
        return initialization;
    }

    public BusinessUniverseInitialization addInformationToInitializationObject(BusinessUniverseInitialization businessUniverseInitialization){
        String currentLogin = guiService.getSessionContext().getCallerPrincipal().getName();
        DomainObject person = personService.findPersonByLogin(currentLogin);
        businessUniverseInitialization.setCurrentLogin(currentLogin);
        businessUniverseInitialization.setFirstName(person.getString("FirstName"));
        businessUniverseInitialization.setLastName(person.getString("LastName"));
        businessUniverseInitialization.seteMail(person.getString("EMail"));
        return businessUniverseInitialization;
    }

    private BusinessUniverseInitialization addLogoImagePath(BusinessUniverseInitialization businessUniverseInitialization) {
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class, "business_universe");
        if (businessUniverseConfig == null) {
            businessUniverseInitialization.setLogoImagePath(DEFAULT_LOGO_PATH);
            return businessUniverseInitialization;
        }
        LogoConfig logoConfig = businessUniverseConfig.getLogoConfig();
        if (logoConfig == null) {
            businessUniverseInitialization.setLogoImagePath(DEFAULT_LOGO_PATH);
            return  businessUniverseInitialization;
        }
        String logoImagePath = logoConfig.getImage();
        businessUniverseInitialization.setLogoImagePath(logoImagePath);
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
