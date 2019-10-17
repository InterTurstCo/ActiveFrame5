package ru.intertrust.cm.core.gui.rpc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.gui.api.server.ApplicationSecurityManager;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploaderServlet;
import ru.intertrust.cm.core.gui.model.Browser;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Client;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.VersionInfo;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.common.versioncollector.ComponentVersion;
import ru.intertrust.common.versioncollector.DiscoveryVersionCollectorService;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService",
        urlPatterns = "/remote/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    private static final String CLIENT_INFO_USER_AGENT_COOKIE = "client.info.user.agent.cookie";
    private static final String CLIENT_INFO_TIME_ZONE_COOKIE = "client.info.time.zone.cookie";
    private static final String DEFAULT_LOGO_PATH = "logo.gif";
    private static final String APPLICATION_URI_ATTRIBUTE = "uri";

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

    @Autowired
    private ApplicationContext applicationContext;

    @EJB
    private UserSettingsFetcher userSettingsFetcher;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization(Client clientInfo) {
        // Информацию user info теперь храним в куках, чтобы небыло ошибок после таймаута сесии
        try {
            getThreadLocalResponse().addCookie(new Cookie(CLIENT_INFO_USER_AGENT_COOKIE, URLEncoder.encode(clientInfo.getDescriptor(), "UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            throw new FatalException("Error set client user agent", ex);
        }
        getThreadLocalResponse().addCookie(new Cookie(CLIENT_INFO_TIME_ZONE_COOKIE, clientInfo.getTimeZoneId()));
        
        UserInfo userInfo = getUserInfo();
        GuiContext.get().setUserInfo(userInfo);

        BusinessUniverseInitialization initialization = new BusinessUniverseInitialization();

        String currentLocale = userInfo.getLocale();
        initialization.setCurrentLocale(currentLocale);
        final BusinessUniverseConfig businessUniverseConfig = configurationService.getLocalizedConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME, currentLocale);

        if (getThreadLocalRequest().getSession().getAttribute(APPLICATION_URI_ATTRIBUTE) != null) {
            String applicationNamePart = getThreadLocalRequest().getSession().getAttribute(APPLICATION_URI_ATTRIBUTE).toString().substring(
                    getThreadLocalRequest().getSession().getAttribute(APPLICATION_URI_ATTRIBUTE).toString().lastIndexOf("/") + 1
            );
            initialization.setApplicationName(applicationNamePart);
        }

        addInformationToInitializationObject(initialization);


        addLogoImagePath(businessUniverseConfig, initialization);

        initialization.setUserExtraInfo(guiService.getUserExtraInfo());

        String version = guiService.getCoreVersion();
        initialization.setApplicationVersion(version);

        addGlobalSettingsRelatedData(initialization);
        if (businessUniverseConfig != null) {
            addSettingsPopupConfig(businessUniverseConfig, initialization);
            addSideBarOpeningTime(businessUniverseConfig, initialization);
            addWidgetConfigs(businessUniverseConfig, initialization);
            addCollectionCountersUpdatePeriod(businessUniverseConfig, initialization);
            addHeaderNotificationPeriod(businessUniverseConfig, initialization);
            addTopPanelConfig(businessUniverseConfig, initialization);
        }
        initialization.setSearchConfigured(isSearchConfigured());
        Map<String, String> messages = MessageResourceProvider.getMessages(currentLocale);
        initialization.setGlobalLocalizedResources(messages);
        DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(false);
        initialization.setInitialNavigationLink(domainObject.getString(UserSettingsHelper.DO_INITIAL_NAVIGATION_LINK_KEY));
        if (initialization.getApplicationName() == null) {
            initialization.setApplicationName(domainObject.getString(UserSettingsHelper.DO_INITIAL_APPLICATION_NAME));
            if(initialization.getApplicationName() == null || initialization.getApplicationName().equals("undefined")) {
                initialization.setApplicationName((businessUniverseConfig != null && businessUniverseConfig.getDefaultAppConfig()!=null)?
                        businessUniverseConfig.getDefaultAppConfig().getValue():"undefined");
            }
        }

        if (businessUniverseConfig != null) {
            initialization.setLoginScreenConfig(businessUniverseConfig.getLoginScreenConfig());
            if (businessUniverseConfig.getLoginScreenConfig().isDisplayVersionList()){
                List<ComponentVersion> componentVersions = DiscoveryVersionCollectorService.INSTANCE.getAllVersions();
                List<VersionInfo> versionInfos = new ArrayList<VersionInfo>();
                for (ComponentVersion componentVersion : componentVersions) {
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setComponent(componentVersion.getComponent());
                    versionInfo.setVersion(componentVersion.getVersion());
                    versionInfos.add(versionInfo);
                }
                initialization.setProductVersionList(versionInfos);
            }
        }

        Boolean hideLogoutButton = (Boolean)getThreadLocalRequest().getSession().getAttribute(ApplicationSecurityManager.HIDE_LOGOUT_BUTTON);
        if (hideLogoutButton != null) {
            initialization.setHideLogoutButton(hideLogoutButton);
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
        try {
            final Pair<String, Boolean> messageInfo = ExceptionMessageFactory.getMessage(command, e instanceof EJBException ? e.getCause() : e,
                    GuiContext.getUserLocale());
            final String message = messageInfo.getFirst();
            final Boolean toLog = messageInfo.getSecond();
            if (toLog) {
                log.error(message, e);
            }
            return new GuiException(message, e);
        } catch (Throwable throwable) {
            log.error("Exception when handling exception...");
            log.error("Original exception", e);
            log.error("Exception while handling", throwable);
            String message;
            try {
                message = MessageResourceProvider.getMessage(LocalizationKeys.SYSTEM_EXCEPTION,
                        GuiContext.getUserLocale(), "Системная ошибка при выполнении, обратитесь к администратору.");
            } catch (Throwable throwable1) {
                message = "Системная ошибка при выполнении, обратитесь к администратору.";
            }
            return new GuiException(message, e);
        }
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
                                       BusinessUniverseInitialization businessUniverseInitialization) {

        if (businessUniverseConfig.getSideBarOpenningTimeConfig() == null) {
            businessUniverseInitialization.setSideBarOpenningTimeConfig(500);
        } else {
            businessUniverseInitialization.setSideBarOpenningTimeConfig(Integer.parseInt(businessUniverseConfig
                    .getSideBarOpenningTimeConfig().getDefaultValue()));
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

    private void addTopPanelConfig(BusinessUniverseConfig businessUniverseConfig,
                                   BusinessUniverseInitialization businessUniverseInitialization){
        businessUniverseInitialization.setTopPanelConfig(businessUniverseConfig.getTopPanelConfig());
    }

    private UserInfo getUserInfo() {
        Client client = getClientInfo();
        
        final UserInfo userInfo = new UserInfo();
        userInfo.setTimeZoneId(client.getTimeZoneId());
        userInfo.setLocale(profileService.getPersonLocale());
        return userInfo;
    }
    
    private Client getClientInfo() {
        final Browser client = new Browser();
        Cookie[] cookies = getThreadLocalRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CLIENT_INFO_TIME_ZONE_COOKIE)) {
                    client.setTimeZoneId(cookie.getValue());
                }else if(cookie.getName().equals(CLIENT_INFO_USER_AGENT_COOKIE)) {
                    try {
                        client.setDescriptor(URLDecoder.decode(cookie.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        throw new FatalException("Error get client user agent", ex);
                    }
                }
            }
        }                
        return client;        
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

    private void addWidgetConfigs(BusinessUniverseConfig businessUniverseConfig,
                                  BusinessUniverseInitialization initialization) {
        initialization.setBottomPanelConfig(businessUniverseConfig.getBottomPanelConfig());
        initialization.setRightPanelConfig(businessUniverseConfig.getRightPanelConfig());

    }

    private void addHeaderNotificationPeriod(BusinessUniverseConfig businessUniverseConfig,
                                             BusinessUniverseInitialization initialization) {
        initialization.setHeaderNotificationPeriod(
                businessUniverseConfig.getHeaderNotificationRefreshConfig() == null
                        ? null
                        : businessUniverseConfig.getHeaderNotificationRefreshConfig().getTime());

    }

    private void addCollectionCountersUpdatePeriod(BusinessUniverseConfig businessUniverseConfig,
                                                   BusinessUniverseInitialization initialization) {
        initialization.setCollectionCountersUpdatePeriod(
                businessUniverseConfig.getCollectionCountRefreshConfig() == null
                        ? null
                        : businessUniverseConfig.getCollectionCountRefreshConfig().getTime());
    }

    private void addGlobalSettingsRelatedData(BusinessUniverseInitialization initialization) {
        GlobalSettingsConfig globalSettingsConfig = configurationService.getGlobalSettings();
        final ApplicationHelpConfig helpConfig = globalSettingsConfig.getApplicationHelpConfig();
        if (helpConfig != null) {
            initialization.setHelperLink(helpConfig.getSource());
        } else {
            initialization.setHelperLink("help/page404.html");
        }
        final ProductTitle productTitle = globalSettingsConfig.getProductTitle();
        initialization.setPageNamePrefix(productTitle == null ? null : productTitle.getTitle() == null ? null
                : productTitle.getTitle() + ": ");
        final ProductVersion productVersion = globalSettingsConfig.getProductVersion();
        initialization.setProductVersion(productVersion == null || productVersion.getArchive() == null ? ""
                : guiService.getProductVersion(productVersion.getArchive()));
    }

    public boolean isSearchConfigured() {
        Collection<SearchAreaConfig> searchAreaConfigs = configurationService.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig searchAreaConfig : searchAreaConfigs) {
            List<TargetDomainObjectConfig> targetObjects = searchAreaConfig.getTargetObjects();
            // список целевых ДО в конкретной области поиска
            ArrayList<String> arrayTargetObjects = new ArrayList<String>();
            for (TargetDomainObjectConfig targetObject : targetObjects) {
                // получаем результирующую форму поиска(удаляем несоответствующие поля)
                List<IndexedFieldConfig> fields = targetObject.getFields();
                ArrayList<String> fieldNames = new ArrayList<String>(fields.size());
                for (IndexedFieldConfig field : fields) {
                    fieldNames.add(field.getName());
                }
                // если форма поиска для данного ДО не сконфигурирована, в интерфейсе не отображается
                final UserInfo userInfo = GuiContext.get().getUserInfo();
                FormDisplayData form = guiService.getSearchForm(targetObject.getType(), new HashSet<String>(fieldNames), userInfo);
                if (form != null) {
                    arrayTargetObjects.add(targetObject.getType());
                }
            }
            // если у области поиска нет сконфигурированной формы для поиска ДО, ее не отображаем
            if (!arrayTargetObjects.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
