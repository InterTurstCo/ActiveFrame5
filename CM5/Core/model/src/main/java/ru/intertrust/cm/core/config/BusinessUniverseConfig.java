package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 17:15
 */
@Root(name = "business-universe")
public class BusinessUniverseConfig implements TopLevelConfig {
    public static final String NAME = "business_universe";

    @Element(name = "login-screen", required = false)
    private LoginScreenConfig loginScreenConfig;

    @Element(name = "side-bar-openning-time", required = false)
    private SideBarOpenningTimeConfig sideBarOpenningTimeConfig;

    @Element(name = "logo", required = false)
    private LogoConfig logoConfig;

    @Element(name = "header-notification-refresh", required = false)
    private HeaderNotificationRefreshConfig headerNotificationRefreshConfig;

    @Element(name = "collection-count-refresh", required = true)
    private CollectionCountRefreshConfig collectionCountRefreshConfig;

    @Element(name = "collection-count-cache-refresh", required = true)
    private CollectionCountCacheRefreshConfig collectionCountCacheRefreshConfig;

    @Element(name = "settings-popup", required = false)
    private SettingsPopupConfig settingsPopupConfig;

    public LogoConfig getLogoConfig() {
        return logoConfig;
    }

    public void setLogoConfig(LogoConfig logoConfig) {
        this.logoConfig = logoConfig;
    }

    public HeaderNotificationRefreshConfig getHeaderNotificationRefreshConfig() {
        return headerNotificationRefreshConfig;
    }

    public LoginScreenConfig getLoginScreenConfig() {
        return loginScreenConfig;
    }

    public void setLoginScreenConfig(LoginScreenConfig loginScreenConfig) {
        this.loginScreenConfig = loginScreenConfig;
    }

    public CollectionCountRefreshConfig getCollectionCountRefreshConfig() {
        return collectionCountRefreshConfig;
    }

    public void setCollectionCountRefreshConfig(CollectionCountRefreshConfig collectionCountRefreshConfig) {
        this.collectionCountRefreshConfig = collectionCountRefreshConfig;
    }

    public CollectionCountCacheRefreshConfig getCollectionCountCacheRefreshConfig() {
        return collectionCountCacheRefreshConfig;
    }

    public void setCollectionCountCacheRefreshConfig(CollectionCountCacheRefreshConfig collectionCountCacheRefreshConfig) {
        this.collectionCountCacheRefreshConfig = collectionCountCacheRefreshConfig;
    }

    public SettingsPopupConfig getSettingsPopupConfig() {
        return settingsPopupConfig;
    }

    public void setSettingsPopupConfig(SettingsPopupConfig settingsPopupConfig) {
        this.settingsPopupConfig = settingsPopupConfig;
    }

    public SideBarOpenningTimeConfig getSideBarOpenningTimeConfig() {
        return sideBarOpenningTimeConfig;
    }

    public void setSideBarOpenningTimeConfig(SideBarOpenningTimeConfig sideBarOpenningTimeConfig) {
        this.sideBarOpenningTimeConfig = sideBarOpenningTimeConfig;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessUniverseConfig that = (BusinessUniverseConfig) o;

        if (collectionCountCacheRefreshConfig != null ? !collectionCountCacheRefreshConfig.equals(that.collectionCountCacheRefreshConfig) : that.collectionCountCacheRefreshConfig != null)
            return false;
        if (collectionCountRefreshConfig != null ? !collectionCountRefreshConfig.equals(that.collectionCountRefreshConfig) : that.collectionCountRefreshConfig != null)
            return false;
        if (headerNotificationRefreshConfig != null ? !headerNotificationRefreshConfig.equals(that.headerNotificationRefreshConfig) : that.headerNotificationRefreshConfig != null)
            return false;
        if (loginScreenConfig != null ? !loginScreenConfig.equals(that.loginScreenConfig) : that.loginScreenConfig != null)
            return false;
        if (logoConfig != null ? !logoConfig.equals(that.logoConfig) : that.logoConfig != null) return false;
        if (settingsPopupConfig != null ? !settingsPopupConfig.equals(that.settingsPopupConfig) : that.settingsPopupConfig != null)
            return false;
        if (sideBarOpenningTimeConfig != null ? !sideBarOpenningTimeConfig.equals(that.sideBarOpenningTimeConfig) : that.sideBarOpenningTimeConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = loginScreenConfig != null ? loginScreenConfig.hashCode() : 0;
        result = 31 * result + (sideBarOpenningTimeConfig != null ? sideBarOpenningTimeConfig.hashCode() : 0);
        result = 31 * result + (logoConfig != null ? logoConfig.hashCode() : 0);
        result = 31 * result + (headerNotificationRefreshConfig != null ? headerNotificationRefreshConfig.hashCode() : 0);
        result = 31 * result + (collectionCountRefreshConfig != null ? collectionCountRefreshConfig.hashCode() : 0);
        result = 31 * result + (collectionCountCacheRefreshConfig != null ? collectionCountCacheRefreshConfig.hashCode() : 0);
        result = 31 * result + (settingsPopupConfig != null ? settingsPopupConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
