package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.2014
 *         Time: 12:12
 */
public class DevelopmentModeConfig implements Dto {
    @Element(name = "logical-validation", required = false)
    private LogicalValidationConfig logicalValidation;

    public LogicalValidationConfig getLogicalValidation() {
        return logicalValidation;
    }

    public void setLogicalValidation(LogicalValidationConfig logicalValidation) {
        this.logicalValidation = logicalValidation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DevelopmentModeConfig that = (DevelopmentModeConfig) o;

        if (logicalValidation != null ? !logicalValidation.equals(that.logicalValidation) : that.logicalValidation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return logicalValidation != null ? logicalValidation.hashCode() : 0;
    }
}
