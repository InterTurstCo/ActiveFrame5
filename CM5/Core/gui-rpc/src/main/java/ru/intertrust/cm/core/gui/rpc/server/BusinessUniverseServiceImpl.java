package ru.intertrust.cm.core.gui.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.ApplicationHelpConfig;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.LogoConfig;
import ru.intertrust.cm.core.config.ProductTitle;
import ru.intertrust.cm.core.config.ProductVersion;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemesConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploaderServlet;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Client;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService",
        urlPatterns = "/remote/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    private static final String CLIENT_INFO_SESSION_ATTRIBUTE = "_CLIENT_INFO";
    private static final String DEFAULT_LOGO_PATH = "logo.gif";

    private static Logger log = LoggerFactory.getLogger(BusinessUniverseServiceImpl.class);

    @EJB
    private GuiService guiService;

    @EJB
    PersonService personService;

    @EJB
    private ConfigurationService configurationService;

    @EJB
    private CollectionsService collectionsService;

    @EJB
    private ProfileService profileService;

    private SoftReference<List<String>> refTimeZoneIds;

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization(Client clientInfo) {
        getThreadLocalRequest().getSession().setAttribute(CLIENT_INFO_SESSION_ATTRIBUTE, clientInfo);

        BusinessUniverseInitialization initialization = new BusinessUniverseInitialization();
        addInformationToInitializationObject(initialization);
        String currentLocale = profileService.getPersonLocale();
        initialization.setCurrentLocale(currentLocale);
        final BusinessUniverseConfig businessUniverseConfig = configurationService.getLocalizedConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME, currentLocale);

        addLogoImagePath(businessUniverseConfig, initialization);
        String version = guiService.getCoreVersion();
        initialization.setApplicationVersion(version);

        GlobalSettingsConfig globalSettingsConfig = configurationService.getGlobalSettings();
        final ApplicationHelpConfig helpConfig = globalSettingsConfig.getApplicationHelpConfig();
        if (helpConfig != null) {
            initialization.setHelperLink(helpConfig.getSource());
        }
        else{
            initialization.setHelperLink("help/page404.html");
        }
        final ProductTitle productTitle = globalSettingsConfig.getProductTitle();
        initialization.setPageNamePrefix(productTitle == null ? null : productTitle.getTitle() == null ? null : productTitle.getTitle() + ": ");
        final ProductVersion productVersion = globalSettingsConfig.getProductVersion();
        initialization.setProductVersion(productVersion == null || productVersion.getArchive() == null ? "" : guiService.getProductVersion(productVersion.getArchive()));

        addSettingsPopupConfig(businessUniverseConfig, initialization);
        addSideBarOpeningTime(businessUniverseConfig, initialization);
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
        Map<String, String> messages = MessageResourceProvider.getMessages(currentLocale);
        initialization.setGlobalLocalizedResources(messages);
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
        final Pair<String, Boolean> messageInfo = ExceptionMessageFactory.getMessage(command, e instanceof EJBException ? e.getCause() : e,
                profileService.getPersonLocale());
        final String message = messageInfo.getFirst();
        final Boolean toLog = messageInfo.getSecond();
        if (toLog) {
            log.error(message, e);
        }
        return new GuiException(message, e);
    }

    @Override
    public FormDisplayData getForm(Id domainObjectId) {
        return guiService.getForm(domainObjectId, getUserInfo(), null);
    }

    @Override
    public AttachmentUploadPercentage getAttachmentUploadPercentage() {

        HttpSession session = getThreadLocalRequest().getSession();
        AttachmentUploadPercentage uploadProgress = AttachmentUploaderServlet.getUploadProgress(session);

        return uploadProgress;
    }

    @Override
    public CollectionCountersResponse getCollectionCounters(CollectionCountersRequest req) {
        return null;
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

    private void addSideBarOpeningTime(BusinessUniverseConfig businessUniverseConfig,
                                       BusinessUniverseInitialization businessUniverseInitialization){

        if(businessUniverseConfig.getSideBarOpenningTimeConfig() == null){
            businessUniverseInitialization.setSideBarOpenningTimeConfig(500);
        }
        else{
            businessUniverseInitialization.setSideBarOpenningTimeConfig(Integer.parseInt(businessUniverseConfig.getSideBarOpenningTimeConfig().getDefaultValue()));
        }

    }

    private void addSettingsPopupConfig(BusinessUniverseConfig businessUniverseConfig,
                                        BusinessUniverseInitialization businessUniverseInitialization) {

        String userTheme = null;
        final IdentifiableObject userSettings = PluginHandlerHelper
                .getUserSettingsIdentifiableObject(guiService.getUserUid(), collectionsService);
        if (userSettings != null) {
            userTheme = userSettings.getString(UserSettingsHelper.DO_THEME_FIELD_KEY);
        }
        if (userTheme != null) {
            final SettingsPopupConfig settingsPopupConfig =
                    businessUniverseConfig == null || businessUniverseConfig.getSettingsPopupConfig() == null
                            ? new SettingsPopupConfig()
                            : businessUniverseConfig.getSettingsPopupConfig();
            final ThemesConfig themesConfig = settingsPopupConfig.getThemesConfig() == null
                    ? new ThemesConfig()
                    : settingsPopupConfig.getThemesConfig();
            themesConfig.setSelectedTheme(userTheme);
            businessUniverseInitialization.setSettingsPopupConfig(settingsPopupConfig);
        } else if (businessUniverseConfig != null) {
            businessUniverseInitialization.setSettingsPopupConfig(businessUniverseConfig.getSettingsPopupConfig());
        }
    }

    private UserInfo getUserInfo() {
        final Client client = (Client) this.getThreadLocalRequest().getSession().getAttribute(CLIENT_INFO_SESSION_ATTRIBUTE);
        final UserInfo userInfo = new UserInfo();
        userInfo.setTimeZoneId(client.getTimeZoneId());
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
