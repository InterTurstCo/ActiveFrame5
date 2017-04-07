package ru.intertrust.cm.core.config.crypto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.CollectorSettingsConverter;

@Root(name = "type-crypto-settings")
public class TypeCryptoSettingsConfig implements TopLevelConfig{
    private static final long serialVersionUID = 1L;

    @Attribute(required = true)
    private String name;

    @Attribute(required = false, name="get-content-bean-name")
    private String getContentBeanName;    

    @Attribute(required = false, name="signature-storage-bean-name")
    private String signatureStorageBeanName;    
    
    /**
     * Конфигурация спринг бина получения подписываемого контента
     */
    @Element(name = "get-content-bean-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings getContentBeanSettings;
    
    /**
     * Конфигурация спринг бина сохранения ЭП
     */
    @Element(name = "signature-storage-bean-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings signatureStorageBeanSettings;    
    
    @Override
    public String getName() {
        return name;
    }    
    
    public void setName(String name) {
        this.name = name;
    }

    public String getGetContentBeanName() {
        return getContentBeanName;
    }

    public void setGetContentBeanName(String getContentBeanName) {
        this.getContentBeanName = getContentBeanName;
    }

    public String getSignatureStorageBeanName() {
        return signatureStorageBeanName;
    }

    public void setSignatureStorageBeanName(String signatureStorageBeanName) {
        this.signatureStorageBeanName = signatureStorageBeanName;
    }

    public CollectorSettings getGetContentBeanSettings() {
        return getContentBeanSettings;
    }

    public void setGetContentBeanSettings(CollectorSettings getContentBeanSettings) {
        this.getContentBeanSettings = getContentBeanSettings;
    }

    public CollectorSettings getSignatureStorageBeanSettings() {
        return signatureStorageBeanSettings;
    }

    public void setSignatureStorageBeanSettings(CollectorSettings signatureStorageBeanSettings) {
        this.signatureStorageBeanSettings = signatureStorageBeanSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeCryptoSettingsConfig that = (TypeCryptoSettingsConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (getContentBeanName != null ? !getContentBeanName.equals(that.getContentBeanName) : that.getContentBeanName != null)
            return false;
        if (signatureStorageBeanName != null ? !signatureStorageBeanName.equals(that.signatureStorageBeanName) : that.signatureStorageBeanName != null)
            return false;
        if (getContentBeanSettings != null ? !getContentBeanSettings.equals(that.getContentBeanSettings) : that.getContentBeanSettings != null)
            return false;
        if (signatureStorageBeanSettings != null ? !signatureStorageBeanSettings.equals(that.signatureStorageBeanSettings) : that.signatureStorageBeanSettings != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
