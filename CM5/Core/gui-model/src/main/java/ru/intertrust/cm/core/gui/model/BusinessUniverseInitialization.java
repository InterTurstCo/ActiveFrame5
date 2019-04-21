package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.LoginScreenConfig;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.TopPanelConfig;
import ru.intertrust.cm.core.config.gui.business.universe.BottomPanelConfig;
import ru.intertrust.cm.core.config.gui.business.universe.RightPanelConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Базовая информация, необходимая при загрузке Business Universe
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 12:37
 */
public class BusinessUniverseInitialization implements Dto {
    private String currentLogin;
    private String firstName;
    private String lastName;
    private String eMail;
    private String logoImagePath;
    private SettingsPopupConfig settingsPopupConfig;
    private TopPanelConfig topPanelConfig;
    private String pageNamePrefix;
    private List<String> timeZoneIds;
    private Integer collectionCountersUpdatePeriod;
    private Integer headerNotificationPeriod;
    private String applicationVersion;
    private String productVersion;
    private Integer SideBarOpenningTimeConfig;
    private String helperLink;
    private String currentLocale;
    private Map<String, String> globalLocalizedResources = new HashMap<>();
    private BottomPanelConfig bottomPanelConfig;
    private RightPanelConfig rightPanelConfig;
    private boolean searchConfigured;
    private UserExtraInfo userExtraInfo;
    private String initialNavigationLink;
    private String applicationName;
    private boolean hideLogoutButton;

    private LoginScreenConfig loginScreenConfig;
    private List<VersionInfo> productVersionList;

    public String getHelperLink() {
        return helperLink;
    }

    public void setHelperLink(String helperLink) {
            this.helperLink = helperLink;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        if(productVersion != null){
            this.productVersion = productVersion;
        }
        else{
            this.productVersion = "";
        }

    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        if(applicationVersion != null){
            this.applicationVersion = applicationVersion;
        }
        else{
            this.applicationVersion = "";
        }

    }

    public String getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(String currentLogin) {
        this.currentLogin = currentLogin == null ? "" : currentLogin;
    }

    public String getPageNamePrefix() {
        return pageNamePrefix;
    }

    public void setPageNamePrefix(String pageNamePrefix) {
        this.pageNamePrefix = pageNamePrefix;
    }

    public List<String> getTimeZoneIds() {
        return timeZoneIds;
    }

    public void setTimeZoneIds(List<String> timeZoneIds) {
        this.timeZoneIds = timeZoneIds;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        if (eMail == null){
            this.eMail = "null@null.com";
        }

        this.eMail = eMail;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? "" : lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? "" : firstName;
    }

    public String getLogoImagePath() {
        return logoImagePath;
    }

    public void setLogoImagePath(String logoImagePath) {
        this.logoImagePath = logoImagePath;
    }

    public SettingsPopupConfig getSettingsPopupConfig() {
        return settingsPopupConfig;
    }

    public void setSettingsPopupConfig(SettingsPopupConfig settingsPopupConfig) {
        this.settingsPopupConfig = settingsPopupConfig;
    }

    public TopPanelConfig getTopPanelConfig() {
        return topPanelConfig;
    }

    public void setTopPanelConfig(TopPanelConfig topPanelConfig) {
        this.topPanelConfig = topPanelConfig;
    }

    public Integer getCollectionCountersUpdatePeriod() {
        return collectionCountersUpdatePeriod;
    }

    public void setCollectionCountersUpdatePeriod(final Integer collectionCountersUpdatePeriod) {
        this.collectionCountersUpdatePeriod = collectionCountersUpdatePeriod;
    }

    public Integer getHeaderNotificationPeriod() {
        return headerNotificationPeriod;
    }

    public void setHeaderNotificationPeriod(final Integer headerNotificationPeriod) {
        this.headerNotificationPeriod = headerNotificationPeriod;
    }

    public Integer getSideBarOpenningTimeConfig() {
        return SideBarOpenningTimeConfig;
    }

    public void setSideBarOpenningTimeConfig(Integer sideBarOpenningTimeConfig) {
        SideBarOpenningTimeConfig = sideBarOpenningTimeConfig;
    }

    public Map<String, String> getGlobalLocalizedResources() {
        return globalLocalizedResources;
    }

    public void setGlobalLocalizedResources(Map<String, String> globalLocalizedResources) {
        this.globalLocalizedResources = globalLocalizedResources;
    }

    public String getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(String currentLocale) {
        this.currentLocale = currentLocale;
    }

    public BottomPanelConfig getBottomPanelConfig() {
        return bottomPanelConfig;
    }

    public void setBottomPanelConfig(BottomPanelConfig bottomPanelConfig) {
        this.bottomPanelConfig = bottomPanelConfig;
    }

    public RightPanelConfig getRightPanelConfig() {
        return rightPanelConfig;
    }

    public void setRightPanelConfig(RightPanelConfig rightPanelConfig) {
        this.rightPanelConfig = rightPanelConfig;
    }

    public boolean isSearchConfigured() {
        return searchConfigured;
    }

    public void setSearchConfigured(boolean searchConfigured) {
        this.searchConfigured = searchConfigured;
    }

    public UserExtraInfo getUserExtraInfo() {
        return userExtraInfo;
    }

    public void setUserExtraInfo(UserExtraInfo userExtraInfo) {
        this.userExtraInfo = userExtraInfo;
    }

    public String getInitialNavigationLink() {
        return initialNavigationLink;
    }

    public void setInitialNavigationLink(String initialNavigationLink) {
        this.initialNavigationLink = initialNavigationLink;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public List<VersionInfo> getProductVersionList() {
        return productVersionList;
    }

    public void setProductVersionList(List<VersionInfo> productVersionList) {
        this.productVersionList = productVersionList;
    }

    public LoginScreenConfig getLoginScreenConfig() {
        return loginScreenConfig;
    }

    public void setLoginScreenConfig(LoginScreenConfig loginScreenConfig) {
        this.loginScreenConfig = loginScreenConfig;
    }

    public boolean isHideLogoutButton() {
        return hideLogoutButton;
    }

    public void setHideLogoutButton(boolean hideLogoutButton) {
        this.hideLogoutButton = hideLogoutButton;
    }
}
