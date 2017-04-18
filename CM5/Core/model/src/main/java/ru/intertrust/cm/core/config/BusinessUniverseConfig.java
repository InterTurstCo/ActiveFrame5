package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.gui.business.universe.BottomPanelConfig;
import ru.intertrust.cm.core.config.gui.business.universe.RightPanelConfig;
import ru.intertrust.cm.core.config.gui.business.universe.UserExtraInfoConfig;
import ru.intertrust.cm.core.config.search.ExtendedSearchPopupConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 17:15
 */
@Root(name = "business-universe")
public class BusinessUniverseConfig implements LocalizableConfig {
    public static final String NAME = "business_universe";

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Element(name = "login-screen", required = false)
    private LoginScreenConfig loginScreenConfig;

    @Element(name = "side-bar-opening-time", required = false)
    private SideBarOpeningTimeConfig sideBarOpenningTimeConfig;

    @Element(name = "logo", required = false)
    private LogoConfig logoConfig;

    @Element(name = "base-url", required = false)
    private BaseUrlConfig baseUrlConfig;

    @Element(name = "notification-sort-order", required = false)
    private NotificationSortOrderConfig notificationSortOrderConfig;

    @Element(name = "default-form-editing-style", required = false)
    private DefaultFormEditingStyleConfig defaultFormEditingStyleConfig;

    @Element(name = "header-notification-refresh", required = false)
    private HeaderNotificationRefreshConfig headerNotificationRefreshConfig;

    @Element(name = "header-notification-limit", required = false)
    private HeaderNotificationLimitConfig headerNotificationLimitConfig;

    @Element(name = "collection-count-refresh", required = true)
    private CollectionCountRefreshConfig collectionCountRefreshConfig;

    @Element(name = "collection-count-cache-refresh", required = true)
    private CollectionCountCacheRefreshConfig collectionCountCacheRefreshConfig;

    @Element(name = "settings-popup", required = false)
    private SettingsPopupConfig settingsPopupConfig;

    @Element(name = "extended-search-popup", required = false)
    private ExtendedSearchPopupConfig extendedSearchPopupConfig;

    @Element(name = "bottom-panel", required = false)
    private BottomPanelConfig bottomPanelConfig;

    @Element(name = "right-panel", required = false)
    private RightPanelConfig rightPanelConfig;

    @Element(name = "user-extra-info", required = false)
    private UserExtraInfoConfig userExtraInfoConfig;

    public LogoConfig getLogoConfig() {
        return logoConfig;
    }

    public void setLogoConfig(LogoConfig logoConfig) {
        this.logoConfig = logoConfig;
    }

    public HeaderNotificationRefreshConfig getHeaderNotificationRefreshConfig() {
        return headerNotificationRefreshConfig;
    }

    public NotificationSortOrderConfig getNotificationSortOrderConfig() {
        return notificationSortOrderConfig;
    }

    public void setNotificationSortOrderConfig(NotificationSortOrderConfig notificationSortOrderConfig) {
        this.notificationSortOrderConfig = notificationSortOrderConfig;
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

    public DefaultFormEditingStyleConfig getDefaultFormEditingStyleConfig() {
        return defaultFormEditingStyleConfig;
    }

    public void setDefaultFormEditingStyleConfig(DefaultFormEditingStyleConfig defaultFormEditingStyleConfig) {
        this.defaultFormEditingStyleConfig = defaultFormEditingStyleConfig;
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

    public SideBarOpeningTimeConfig getSideBarOpenningTimeConfig() {
        return sideBarOpenningTimeConfig;
    }

    public void setSideBarOpenningTimeConfig(SideBarOpeningTimeConfig sideBarOpenningTimeConfig) {
        this.sideBarOpenningTimeConfig = sideBarOpenningTimeConfig;
    }

    public ExtendedSearchPopupConfig getExtendedSearchPopupConfig() {
        return extendedSearchPopupConfig;
    }

    public void setExtendedSearchPopupConfig(ExtendedSearchPopupConfig extendedSearchPopupConfig) {
        this.extendedSearchPopupConfig = extendedSearchPopupConfig;
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

    public UserExtraInfoConfig getUserExtraInfoConfig() {
        return userExtraInfoConfig;
    }

    public void setUserExtraInfoConfig(UserExtraInfoConfig userExtraInfoConfig) {
        this.userExtraInfoConfig = userExtraInfoConfig;
    }

    public BaseUrlConfig getBaseUrlConfig() {
        return baseUrlConfig;
    }

    public void setBaseUrlConfig(BaseUrlConfig baseUrlConfig) {
        this.baseUrlConfig = baseUrlConfig;
    }

    public HeaderNotificationLimitConfig getHeaderNotificationLimitConfig() {
        return headerNotificationLimitConfig;
    }

    public void setHeaderNotificationLimitConfig(HeaderNotificationLimitConfig headerNotificationLimitConfig) {
        this.headerNotificationLimitConfig = headerNotificationLimitConfig;
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

        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (bottomPanelConfig != null ? !bottomPanelConfig.equals(that.bottomPanelConfig) : that.bottomPanelConfig != null){
            return false;
        }
        if (collectionCountCacheRefreshConfig != null ? !collectionCountCacheRefreshConfig.equals(that.collectionCountCacheRefreshConfig)
                : that.collectionCountCacheRefreshConfig != null) {
            return false;
        }
        if (collectionCountRefreshConfig != null ? !collectionCountRefreshConfig.equals(that.collectionCountRefreshConfig)
                : that.collectionCountRefreshConfig != null) {
            return false;
        }
        if (extendedSearchPopupConfig != null ? !extendedSearchPopupConfig.equals(that.extendedSearchPopupConfig)
                : that.extendedSearchPopupConfig != null){
            return false;
        }
        if (headerNotificationRefreshConfig != null ? !headerNotificationRefreshConfig.equals(that.headerNotificationRefreshConfig)
                : that.headerNotificationRefreshConfig != null) {
            return false;
        }
        if (loginScreenConfig != null ? !loginScreenConfig.equals(that.loginScreenConfig) : that.loginScreenConfig != null) {
            return false;
        }
        if (logoConfig != null ? !logoConfig.equals(that.logoConfig) : that.logoConfig != null) {
            return false;
        }
        if (baseUrlConfig != null ? !baseUrlConfig.equals(that.baseUrlConfig) : that.baseUrlConfig != null) {
            return false;
        }
        if (notificationSortOrderConfig != null ? !notificationSortOrderConfig.equals(that.notificationSortOrderConfig) : that.notificationSortOrderConfig != null) {
            return false;
        }
        if (rightPanelConfig != null ? !rightPanelConfig.equals(that.rightPanelConfig) : that.rightPanelConfig != null) {
            return false;
        }
        if (settingsPopupConfig != null ? !settingsPopupConfig.equals(that.settingsPopupConfig)
                : that.settingsPopupConfig != null) {
            return false;
        }
        if (sideBarOpenningTimeConfig != null ? !sideBarOpenningTimeConfig.equals(that.sideBarOpenningTimeConfig)
                : that.sideBarOpenningTimeConfig != null) {
            return false;
        }
        if (defaultFormEditingStyleConfig != null ? !defaultFormEditingStyleConfig.equals(that.defaultFormEditingStyleConfig)
                : that.defaultFormEditingStyleConfig != null) {
            return false;
        }
        if (headerNotificationLimitConfig != null ? !defaultFormEditingStyleConfig.equals(that.defaultFormEditingStyleConfig)
                : that.defaultFormEditingStyleConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = loginScreenConfig != null ? loginScreenConfig.hashCode() : 0;
        result = 31 * result + (sideBarOpenningTimeConfig != null ? sideBarOpenningTimeConfig.hashCode() : 0);
        result = 31 * result + (logoConfig != null ? logoConfig.hashCode() : 0);
        result = 31 * result + (baseUrlConfig != null ? baseUrlConfig.hashCode() : 0);
        result = 31 * result + (notificationSortOrderConfig != null ? notificationSortOrderConfig.hashCode() : 0);
        result = 31 * result + (headerNotificationRefreshConfig != null ? headerNotificationRefreshConfig.hashCode() : 0);
        result = 31 * result + (collectionCountRefreshConfig != null ? collectionCountRefreshConfig.hashCode() : 0);
        result = 31 * result + (collectionCountCacheRefreshConfig != null ? collectionCountCacheRefreshConfig.hashCode() : 0);
        result = 31 * result + (settingsPopupConfig != null ? settingsPopupConfig.hashCode() : 0);
        result = 31 * result + (defaultFormEditingStyleConfig != null ? defaultFormEditingStyleConfig.hashCode() : 0);
        result = 31 * result + (headerNotificationLimitConfig != null ? headerNotificationLimitConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }
}
