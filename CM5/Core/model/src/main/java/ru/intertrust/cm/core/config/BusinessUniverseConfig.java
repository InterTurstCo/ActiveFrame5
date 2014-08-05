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

    @Element(name = "login-screen", required = false)
    private LoginScreenConfig loginScreenConfig;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BusinessUniverseConfig that = (BusinessUniverseConfig) o;
        if (logoConfig != null ? !logoConfig.equals(that.logoConfig) : that.logoConfig != null) {
            return false;
        }
        if (headerNotificationRefreshConfig == null
                ? that.headerNotificationRefreshConfig != null
                : !headerNotificationRefreshConfig.equals(that.getHeaderNotificationRefreshConfig())) {
            return false;
        }
        if (!collectionCountRefreshConfig.equals(that.collectionCountRefreshConfig)) {
            return false;
        }
        if (!collectionCountCacheRefreshConfig.equals(that.collectionCountCacheRefreshConfig)) {
            return false;
        }
        if (settingsPopupConfig != null ? !settingsPopupConfig.equals(that.settingsPopupConfig) :
                that.settingsPopupConfig != null) {
            return false;
        }
        if (loginScreenConfig != null ? !loginScreenConfig.equals(that.loginScreenConfig) : that.loginScreenConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (logoConfig != null ? logoConfig.hashCode() : 31);
        result = result * 31 +
                (headerNotificationRefreshConfig == null ? 31 : headerNotificationRefreshConfig.hashCode());
        result = 31 * result + collectionCountRefreshConfig.hashCode();
        result = 31 * result + collectionCountCacheRefreshConfig.hashCode();
        result = 31 * result + (settingsPopupConfig == null ? 31 : settingsPopupConfig.hashCode());

                result = result * 31 +
                (loginScreenConfig == null ? 31 : loginScreenConfig.hashCode());
        return result;
    }

    @Override
    public String getName() {
        return "business_universe";
    }
}
