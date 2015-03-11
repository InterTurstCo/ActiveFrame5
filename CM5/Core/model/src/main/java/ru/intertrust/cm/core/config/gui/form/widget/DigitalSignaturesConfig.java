package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 11.03.2015
 *         Time: 13:53
 */
public class DigitalSignaturesConfig implements Dto {

    @ElementList(inline = true)
    private List<DigitalSignatureConfig> digitalSignatureConfigs = new ArrayList<>();

    public List<DigitalSignatureConfig> getDigitalSignatureConfigs() {
        return digitalSignatureConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DigitalSignaturesConfig that = (DigitalSignaturesConfig) o;
        if (!digitalSignatureConfigs.equals(that.digitalSignatureConfigs)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return digitalSignatureConfigs.hashCode();
    }
}
