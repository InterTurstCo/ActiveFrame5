package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.SettingsPopupConfig;

import java.util.List;

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
    private String pageNamePrefix;
    private List<String> timeZoneIds;
    private Integer collectionCountersUpdatePeriod;
    private Integer headerNotificationPeriod;
    private String applicationVersion;
    private String productVersion;
    private Integer SideBarOpenningTimeConfig;
    private String helperLink;


    public String getHelperLink() {
        return helperLink;
    }

    public void setHelperLink(String helperLink) {
        if(helperLink != null){
            this.helperLink = helperLink;
        }
        else{
            this.helperLink = "help/page404.html";
        }
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
        if (currentLogin == null){
            this.currentLogin = "";
        }
        this.currentLogin = currentLogin;
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
        if (lastName == null){
            this.lastName = "";
        }
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null){
            this.firstName = "";
        }
        this.firstName = firstName;
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
}
