package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Конфигурация матрицы доступа.
 * @author atsvetkov
 *
 */
@Root(name = "access-matrix")
public class AccessMatrixConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String type;

    @Attribute(name= "read-everybody", required = false)
    private Boolean readEverybody; // change to boolean after elimination of permit-everybody attribute in <read> tag

    @ElementList(inline = true, type = AccessMatrixStatusConfig.class, entry = "status", required = false)
    private List<AccessMatrixStatusConfig> status = new ArrayList<>();

    @Attribute(name= "matrix-reference", required = false)
    private String matrixReference;

    
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessMatrixConfig that = (AccessMatrixConfig) o;

        if (readEverybody != null ? !readEverybody.equals(that.readEverybody) : that.readEverybody != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (readEverybody != null ? readEverybody.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return type;
    }

    public String getMatrixReference() {
        return matrixReference;
    }

    public void setMatrixReference(String matrixReference) {
        this.matrixReference = matrixReference;
    }
}
