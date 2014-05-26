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

        if (!collectionCountCacheRefreshConfig.equals(that.collectionCountCacheRefreshConfig)) {
            return false;
        }
        if (!collectionCountRefreshConfig.equals(that.collectionCountRefreshConfig)) {
            return false;
        }
        if (logoConfig != null ? !logoConfig.equals(that.logoConfig) : that.logoConfig != null) {
            return false;
        }
        if (settingsPopupConfig != null ? !settingsPopupConfig.equals(that.settingsPopupConfig) :
                that.settingsPopupConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = logoConfig != null ? logoConfig.hashCode() : 0;
        result = 31 * result + collectionCountRefreshConfig.hashCode();
        result = 31 * result + collectionCountCacheRefreshConfig.hashCode();
        result = 31 * result + settingsPopupConfig.hashCode();
        return result;
    }

    @Override
    public String getName() {
        return "business_universe";
    }
}
