package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
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

    public boolean validateGui() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateGui();
    }

    public boolean validateAccessMatrices() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateAccessMatrices();
    }

    public boolean validateIndirectPermissions() {
        return devModeValidationRulesNotDefined() || developmentMode.getLogicalValidation().validateIndirectPermissions();
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

        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) return false;
        if (productTitle != null ? !productTitle.equals(that.productTitle) : that.productTitle != null) return false;
        if (productVersion != null ? !productVersion.equals(that.productVersion) : that.productVersion != null)
            return false;
        if (searchLanguages != null ? !searchLanguages.equals(that.searchLanguages) : that.searchLanguages != null)
            return false;
        if (transactionTrace != null ? !transactionTrace.equals(that.transactionTrace) : that.transactionTrace != null)
            return false;
        if (developmentMode != null ? !developmentMode.equals(that.developmentMode) : that.developmentMode != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = productTitle != null ? productTitle.hashCode() : 0;
        result = 31 * result + (productVersion != null ? productVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return "";
    }
}
