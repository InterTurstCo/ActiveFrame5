package ru.intertrust.cm.core.config.model;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.model.base.TopLevelConfig;

/**
 * Конфигурация матрицы доступа.
 * @author atsvetkov
 * 
 */
@Root(name = "access-matrix")
public class AccessMatrixConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String type;

    @ElementList(inline = true)
    private List<AccessMatrixStatusConfig> status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return type;
    }
}
