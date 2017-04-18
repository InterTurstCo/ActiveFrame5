package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.migration.MigrationModuleConfig;
import ru.intertrust.cm.core.config.search.SearchLanguageConfig;

import java.util.List;

@Root(name = "global-settings")
public class GlobalSettingsConfig implements TopLevelConfig {
    /**
     *
     */
    private static final long serialVersionUID = -8166587368979922484L;
    public static final String NAME = "global-settings";

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;
    
    @Element(name = "product", required = false)
    private ProductTitle productTitle;

    @Element(name = "product-version", required = false)
    private ProductVersion productVersion;

    @Element(name = "audit-log", required = true)
    private AuditLog auditLog;

    @Element(name = "transaction-trace", required = false)
    private TransactionTrace transactionTrace;

    @ElementList(name = "search-languages", entry = "language", required = false)
    private List<SearchLanguageConfig> searchLanguages;

    @Element(name = "development-mode", required = false)
    private DevelopmentModeConfig developmentMode;

    @ElementList(name = "migration-modules", entry = "module", required = false)
    private List<MigrationModuleConfig> migrationModules;

    @Element(name = "event-logs", required = false)
    private EventLogsConfig eventLogsConfig;

    @Element(name = "application-help", required = false)
    private ApplicationHelpConfig applicationHelpConfig;

    @Element(name = "crypto-settings", required = false)
    private CryptoSettingsConfig cryptoSettingsConfig;

    @Element(name = "collection-query-cache", required = false)
    private CollectionQueryCacheConfig collectionQueryCache;    
    
    @Element(name="default-locale", required = false)
    private DefaultLocaleConfig defaultLocaleConfig;

    public ApplicationHelpConfig getApplicationHelpConfig() {
        return applicationHelpConfig;
    }

    public void setApplicationHelpConfig(ApplicationHelpConfig applicationHelpConfig) {
        this.applicationHelpConfig = applicationHelpConfig;
    }
    
    public CollectionQueryCacheConfig getCollectionQueryCacheConfig() {
        return collectionQueryCache;
    }

    public void setCollectionQueryCacheConfig(CollectionQueryCacheConfig collectionQueryCache) {
        this.collectionQueryCache = collectionQueryCache;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public TransactionTrace getTransactionTrace() {
        return transactionTrace;
    }

    public void setTransactionTrace(TransactionTrace transactionTrace) {
        this.transactionTrace = transactionTrace;
    }

    public List<SearchLanguageConfig> getSearchLanguages() {
        return searchLanguages;
    }

    public List<MigrationModuleConfig> getMigrationModules() {
        return migrationModules;
    }

    public ProductTitle getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(ProductTitle productTitle) {
        this.productTitle = productTitle;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    public DevelopmentModeConfig getDevelopmentMode() {
        return developmentMode;
    }

    public void setDevelopmentMode(DevelopmentModeConfig developmentMode) {
        this.developmentMode = developmentMode;
    }

    public EventLogsConfig getEventLogsConfig() {
        return eventLogsConfig;
    }

    public void setEventLogsConfig(EventLogsConfig eventLogsConfig) {
        this.eventLogsConfig = eventLogsConfig;
    }

    public CryptoSettingsConfig getCryptoSettingsConfig() {
        return cryptoSettingsConfig;
    }

    public void setCryptoSettingsConfig(CryptoSettingsConfig cryptoSettingsConfig) {
        this.cryptoSettingsConfig = cryptoSettingsConfig;
    }

    public boolean validateGui() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateGui();
    }

    public boolean validateAccessMatrices() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateAccessMatrices();
    }

    public boolean validateIndirectPermissions() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateIndirectPermissions();
    }

    public DefaultLocaleConfig getDefaultLocaleConfig() {
        return defaultLocaleConfig;
    }

    private boolean devModeValidationRulesNotDefined() {
        if (developmentMode == null) {
            return true;
        }
        final LogicalValidationConfig logicalValidationConfig = developmentMode.getLogicalValidation();
        if (logicalValidationConfig == null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalSettingsConfig that = (GlobalSettingsConfig) o;

        if (productTitle != null ? !productTitle.equals(that.productTitle) : that.productTitle != null) return false;
        if (productVersion != null ? !productVersion.equals(that.productVersion) : that.productVersion != null)
            return false;
        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) return false;
        if (transactionTrace != null ? !transactionTrace.equals(that.transactionTrace) : that.transactionTrace != null)
            return false;
        if (searchLanguages != null ? !searchLanguages.equals(that.searchLanguages) : that.searchLanguages != null)
            return false;
        if (developmentMode != null ? !developmentMode.equals(that.developmentMode) : that.developmentMode != null)
            return false;
        if (migrationModules != null ? !migrationModules.equals(that.migrationModules) : that.migrationModules != null)
            return false;
        if (eventLogsConfig != null ? !eventLogsConfig.equals(that.eventLogsConfig) : that.eventLogsConfig != null)
            return false;
        if (applicationHelpConfig != null ? !applicationHelpConfig.equals(that.applicationHelpConfig) : that.applicationHelpConfig != null)
            return false;
        if (cryptoSettingsConfig != null ? !cryptoSettingsConfig.equals(that.cryptoSettingsConfig) : that.cryptoSettingsConfig != null)
            return false;
        if (collectionQueryCache != null ? !collectionQueryCache.equals(that.collectionQueryCache) : that.collectionQueryCache != null)
            return false;
        if (defaultLocaleConfig != null ? !defaultLocaleConfig.equals(that.defaultLocaleConfig) : that.defaultLocaleConfig != null)
            return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return productTitle != null ? productTitle.hashCode() : 0;
    }

    @Override
    public String getName() {
        return "";
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
