package ru.intertrust.cm.core.config;

import org.simpleframework.xml.*;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Конфигурация матрицы доступа.
 * @author atsvetkov
 *
 */
@Root(name = "access-matrix")
@Order(elements={"create", "status", "matrix-reference-mapping"})
public class AccessMatrixConfig implements TopLevelConfig {    

    private static final long serialVersionUID = -5099056815437308778L;

    @Attribute(required = true)
    private String type;

    @Attribute(name= "read-everybody", required = false)
    private Boolean readEverybody; // change to boolean after elimination of permit-everybody attribute in <read> tag

    @ElementList(inline = true, type = AccessMatrixStatusConfig.class, entry = "status", required = false)
    private List<AccessMatrixStatusConfig> status = new ArrayList<>();

    @Attribute(name= "matrix-reference-field", required = false)
    private String matrixReference;

    @Attribute(name= "borrow-permissisons", required = false)
    private BorrowPermissisonsMode borrowPermissisons;
    
    @Element(name = "create", required = false)
    private AccessMatrixCreateConfig accessMatrixCreateConfig;

    @Element(name = "matrix-reference-mapping", required = false)
    private MatrixReferenceMappingConfig matrixReferenceMappingConfig;

    @Attribute(name= "support-security-stamp", required = false)
    private Boolean supportSecurityStamp;

    public static enum BorrowPermissisonsMode{
        none,
        read,
        readWriteDelete,
        all
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isReadEverybody() {
        return readEverybody;
    }

    public void setReadEverybody(Boolean readEverybody) {
        this.readEverybody = readEverybody;
    }

    public List<AccessMatrixStatusConfig> getStatus() {
        return status;
    }

    public void setStatus(List<AccessMatrixStatusConfig> status) {
        this.status = status;
    }

    
    public AccessMatrixCreateConfig getCreateConfig() {
        return accessMatrixCreateConfig;
    }

    public void setCreateConfig(AccessMatrixCreateConfig accessMatrixCreateConfig) {
        this.accessMatrixCreateConfig = accessMatrixCreateConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessMatrixConfig that = (AccessMatrixConfig) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(readEverybody, that.readEverybody) &&
                Objects.equals(status, that.status) &&
                Objects.equals(matrixReference, that.matrixReference) &&
                borrowPermissisons == that.borrowPermissisons &&
                Objects.equals(accessMatrixCreateConfig, that.accessMatrixCreateConfig) &&
                Objects.equals(matrixReferenceMappingConfig, that.matrixReferenceMappingConfig) &&
                Objects.equals(supportSecurityStamp, that.supportSecurityStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, readEverybody, status, matrixReference, borrowPermissisons, accessMatrixCreateConfig, matrixReferenceMappingConfig, supportSecurityStamp);
    }

    @Override
    public String getName() {
        return type;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
    }

    public String getMatrixReference() {
        return matrixReference;
    }

    public void setMatrixReference(String matrixReference) {
        this.matrixReference = matrixReference;
    }

    public MatrixReferenceMappingConfig getMatrixReferenceMappingConfig() {
        return matrixReferenceMappingConfig;
    }

    public void setMatrixReferenceMappingConfig(MatrixReferenceMappingConfig matrixReferenceMappingConfig) {
        this.matrixReferenceMappingConfig = matrixReferenceMappingConfig;
    }

    public BorrowPermissisonsMode getBorrowPermissisons() {
        return borrowPermissisons;
    }

    public void setBorrowPermissisons(BorrowPermissisonsMode borrowPermissisons) {
        this.borrowPermissisons = borrowPermissisons;
    }

    @Override
    public String toString() {
        return "AccessMatrixConfig{" +
                "type='" + type + '\'' +
                ", readEverybody=" + readEverybody +
                ", status=" + status +
                ", matrixReference='" + matrixReference + '\'' +
                ", borrowPermissisons=" + borrowPermissisons +
                ", accessMatrixCreateConfig=" + accessMatrixCreateConfig +
                ", matrixReferenceMappingConfig=" + matrixReferenceMappingConfig +
                ", supportSecurityStamp=" + supportSecurityStamp +
                '}';
    }

    public Boolean isSupportSecurityStamp() {
        return supportSecurityStamp;
    }

    public void setSupportSecurityStamp(Boolean supportSecurityStamp) {
        this.supportSecurityStamp = supportSecurityStamp;
    }
}
