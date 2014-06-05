package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
@Root(name = "accepted-types")
public class AcceptedTypesConfig implements Dto {
    @ElementList(inline = true, name ="accepted-type", required = true)
    private List<AcceptedTypeConfig> acceptedTypeConfigs;

    public List<AcceptedTypeConfig> getAcceptedTypeConfigs() {
        return acceptedTypeConfigs;
    }

    public void setAcceptedTypeConfigs(List<AcceptedTypeConfig> acceptedTypeConfigs) {
        this.acceptedTypeConfigs = acceptedTypeConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AcceptedTypesConfig that = (AcceptedTypesConfig) o;

        if (acceptedTypeConfigs != null ? !acceptedTypeConfigs.equals(that.acceptedTypeConfigs) :
                that.acceptedTypeConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return acceptedTypeConfigs != null ? acceptedTypeConfigs.hashCode() : 0;
    }
}
