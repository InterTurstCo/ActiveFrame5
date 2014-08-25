package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.2014
 *         Time: 12:11
 */
public class LogicalValidationConfig implements Dto {
    @Attribute(name = "access-matrices", required = false)
    private boolean validateAccessMatrices = true;

    @Attribute(name = "indirect-permissions", required = false)
    private boolean validateIndirectPermissions = true;

    @Attribute(name = "gui", required = false)
    private boolean validateGui = true;

    public boolean validateAccessMatrices() {
        return validateAccessMatrices;
    }

    public void setValidateAccessMatrices(boolean validateAccessMatrices) {
        this.validateAccessMatrices = validateAccessMatrices;
    }

    public boolean validateIndirectPermissions() {
        return validateIndirectPermissions;
    }

    public void setValidateIndirectPermissions(boolean validateIndirectPermissions) {
        this.validateIndirectPermissions = validateIndirectPermissions;
    }

    public boolean validateGui() {
        return validateGui;
    }

    public void setValidateGui(boolean validateGui) {
        this.validateGui = validateGui;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogicalValidationConfig that = (LogicalValidationConfig) o;

        if (validateAccessMatrices != that.validateAccessMatrices) {
            return false;
        }
        if (validateGui != that.validateGui) {
            return false;
        }
        if (validateIndirectPermissions != that.validateIndirectPermissions) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (validateAccessMatrices ? 1 : 0);
        result = 31 * result + (validateIndirectPermissions ? 1 : 0);
        result = 31 * result + (validateGui ? 1 : 0);
        return result;
    }
}
