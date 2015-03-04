package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.search.SearchLanguageConfig;

import java.util.List;

@Root(name = "global-settings")
public class GlobalSettingsConfig implements TopLevelConfig {
    /**
     *
     */
    private static final long serialVersionUID = -8166587368979922484L;
    public static final String NAME = "global-settings";

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

    @Element(name = "event-logs", required = false)
    private EventLogsConfig eventLogsConfig;

    @Element(name = "application-help", required = false)
    private ApplicationHelpConfig applicationHelpConfig;

    @Element(name="default-locale", required = false)
    private DefaultLocaleConfig defaultLocaleConfig;

    public ApplicationHelpConfig getApplicationHelpConfig() {
        return applicationHelpConfig;
    }

    public void setApplicationHelpConfig(ApplicationHelpConfig applicationHelpConfig) {
        this.applicationHelpConfig = applicationHelpConfig;
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

        if (applicationHelpConfig != null ? !applicationHelpConfig.equals(that.applicationHelpConfig) : that.applicationHelpConfig != null)
            return false;
        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) return false;
        if (developmentMode != null ? !developmentMode.equals(that.developmentMode) : that.developmentMode != null)
            return false;
        if (eventLogsConfig != null ? !eventLogsConfig.equals(that.eventLogsConfig) : that.eventLogsConfig != null)
            return false;
        if (productTitle != null ? !productTitle.equals(that.productTitle) : that.productTitle != null) return false;
        if (productVersion != null ? !productVersion.equals(that.productVersion) : that.productVersion != null)
            return false;
        if (searchLanguages != null ? !searchLanguages.equals(that.searchLanguages) : that.searchLanguages != null)
            return false;
        if (transactionTrace != null ? !transactionTrace.equals(that.transactionTrace) : that.transactionTrace != null)
            return false;
        if (defaultLocaleConfig != null ? !defaultLocaleConfig.equals(that.defaultLocaleConfig) : that.defaultLocaleConfig != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = productTitle != null ? productTitle.hashCode() : 0;
        result = 31 * result + (productVersion != null ? productVersion.hashCode() : 0);
        result = 31 * result + (auditLog != null ? auditLog.hashCode() : 0);
        result = 31 * result + (transactionTrace != null ? transactionTrace.hashCode() : 0);
        result = 31 * result + (searchLanguages != null ? searchLanguages.hashCode() : 0);
        result = 31 * result + (developmentMode != null ? developmentMode.hashCode() : 0);
        result = 31 * result + (eventLogsConfig != null ? eventLogsConfig.hashCode() : 0);
        result = 31 * result + (applicationHelpConfig != null ? applicationHelpConfig.hashCode() : 0);
        result = 31 * result + (defaultLocaleConfig != null ? defaultLocaleConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return "";
    }
}
