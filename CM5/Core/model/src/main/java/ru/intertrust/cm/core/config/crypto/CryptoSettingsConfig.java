package ru.intertrust.cm.core.config.crypto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.converter.CollectorSettingsConverter;

public class CryptoSettingsConfig implements Dto {
    private static final long serialVersionUID = -5759794032403165214L;

    @Attribute(name = "gui-component-name", required = true)
    private String guiComponentName;

    @Attribute(name = "server-signature-verifier-bean-name", required = true)
    private String serverSignatureVerifierBeanName;

    @Element(name = "provider-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings settings;

    public String getGuiComponentName() {
        return guiComponentName;
    }

    public void setGuiComponentName(String guiComponentName) {
        this.guiComponentName = guiComponentName;
    }

    public String getServerSignatureVerifierBeanName() {
        return serverSignatureVerifierBeanName;
    }

    public void setServerSignatureVerifierBeanName(String serverSignatureVerifierBeanName) {
        this.serverSignatureVerifierBeanName = serverSignatureVerifierBeanName;
    }

    public CollectorSettings getSettings() {
        return settings;
    }

    public void setSettings(CollectorSettings settings) {
        this.settings = settings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((guiComponentName == null) ? 0 : guiComponentName.hashCode());
        result = prime * result + ((serverSignatureVerifierBeanName == null) ? 0 : serverSignatureVerifierBeanName.hashCode());
        result = prime * result + ((settings == null) ? 0 : settings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CryptoSettingsConfig other = (CryptoSettingsConfig) obj;
        if (guiComponentName == null) {
            if (other.guiComponentName != null)
                return false;
        } else if (!guiComponentName.equals(other.guiComponentName))
            return false;
        if (serverSignatureVerifierBeanName == null) {
            if (other.serverSignatureVerifierBeanName != null)
                return false;
        } else if (!serverSignatureVerifierBeanName.equals(other.serverSignatureVerifierBeanName))
            return false;
        if (settings == null) {
            if (other.settings != null)
                return false;
        } else if (!settings.equals(other.settings))
            return false;
        return true;
    }
}
