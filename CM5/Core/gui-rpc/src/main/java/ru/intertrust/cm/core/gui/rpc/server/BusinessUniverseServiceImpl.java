package ru.intertrust.cm.core.gui.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.LogoConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploaderServlet;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService",
        urlPatterns = "/remote/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    private static final String DEFAULT_LOGO_PATH = "logo.gif";
    private static final String DEFAULT_THEME_NAME = "default";
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessUniverseServiceImpl.class);
    @EJB
    private GuiService guiService;

    @EJB
    PersonService personService;

    @EJB
    private ConfigurationService configurationService;

    private SoftReference<List<String>> refTimeZoneIds;

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization() {
        BusinessUniverseInitialization initialization = new BusinessUniverseInitialization();
        addInformationToInitializationObject(initialization);
        final BusinessUniverseConfig businessUniverseConfig =
                configurationService.getConfig(BusinessUniverseConfig.class, "business_universe");
        addLogoImagePath(businessUniverseConfig, initialization);
        addSettingsPopupConfig(businessUniverseConfig, initialization);
        if (businessUniverseConfig != null) {
            initialization.setCollectionCountersUpdatePeriod(
                    businessUniverseConfig.getCollectionCountRefreshConfig() == null
                    ? null
                    : businessUniverseConfig.getCollectionCountRefreshConfig().getTime());
            initialization.setHeaderNotificationPeriod(
                    businessUniverseConfig.getHeaderNotificationRefreshConfig() == null
                    ? null
                    : businessUniverseConfig.getHeaderNotificationRefreshConfig().getTime());
        }
        return initialization;
    }

    public BusinessUniverseInitialization addInformationToInitializationObject(
            BusinessUniverseInitialization businessUniverseInitialization) {
        final String currentLogin = guiService.getUserUid();
        final DomainObject person = personService.findPersonByLogin(currentLogin);
        businessUniverseInitialization.setCurrentLogin(currentLogin);
        businessUniverseInitialization.setFirstName(person.getString("FirstName"));
        businessUniverseInitialization.setLastName(person.getString("LastName"));
        businessUniverseInitialization.seteMail(person.getString("EMail"));
        businessUniverseInitialization.setTimeZoneIds(getTimeZoneIds());
        return businessUniverseInitialization;
    }

    @Override
    public Dto executeCommand(Command command) throws GuiException {
        try {
            return guiService.executeCommand(command, getUserInfo());
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
        return guiService.getForm(domainObjectId, getUserInfo());
    }

    @Override
    public AttachmentUploadPercentage getAttachmentUploadPercentage() {

        HttpSession session = getThreadLocalRequest().getSession();
        AttachmentUploadPercentage uploadProgress = AttachmentUploaderServlet.getUploadProgress(session);

        return uploadProgress;
    }

    private void addLogoImagePath(final BusinessUniverseConfig businessUniverseConfig,
                                                            BusinessUniverseInitialization businessUniverseInitialization) {
        if (businessUniverseConfig == null) {
            businessUniverseInitialization.setLogoImagePath(DEFAULT_LOGO_PATH);
            return;
        }
        LogoConfig logoConfig = businessUniverseConfig.getLogoConfig();
        if (logoConfig == null) {
            businessUniverseInitialization.setLogoImagePath(DEFAULT_LOGO_PATH);
            return;
        }
        String logoImagePath = logoConfig.getImage();
        businessUniverseInitialization.setLogoImagePath(logoImagePath);
        return;
    }
    private void addSettingsPopupConfig(BusinessUniverseConfig businessUniverseConfig,
                                        BusinessUniverseInitialization businessUniverseInitialization) {
        if (businessUniverseConfig != null) {
                   businessUniverseInitialization.setSettingsPopupConfig(businessUniverseConfig.getSettingsPopupConfig());
        }

    }

    private UserInfo getUserInfo() {
        final UserInfo userInfo = new UserInfo();
        final UserUidWithPassword userUidWithPassword = (UserUidWithPassword) this.getThreadLocalRequest()
                .getSession().getAttribute(LoginServiceImpl.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        userInfo.setTimeZoneId(userUidWithPassword.getClientTimeZone());
        return userInfo;
    }

    private List<String> getTimeZoneIds() {
        List<String> result = refTimeZoneIds == null ? null : refTimeZoneIds.get();
        if (result == null) {
            final Set<String> timeZoneIdSet = new LinkedHashSet<>(40);
            timeZoneIdSet.addAll(Arrays.asList("По умолчанию", "Локальная", "Оригинальная"));
            final String[] timeZoneIds = TimeZone.getAvailableIDs();
            for (String timeZoneId : timeZoneIds) {
                final TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
                timeZoneIdSet.add(ModelUtil.getUTCTimeZoneId(timeZone.getRawOffset()));
            }
            result = new ArrayList<>(timeZoneIdSet);
            refTimeZoneIds = new SoftReference<>(result);
        }
        return result;
    }
}
