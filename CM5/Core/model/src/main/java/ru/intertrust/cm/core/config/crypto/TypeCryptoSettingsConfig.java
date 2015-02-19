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

    @Attribute(required = true, name="get-content-bean-name")
    private String getContentBeanName;    

    @Attribute(required = true, name="save-signature-bean-name")
    private String saveSignatureBeanName;    
    
    /**
     * Конфигурация спринг бина получения подписываемого контента
     */
    @Element(name = "get-content-bean-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings getContentBeanSettings;
    
    /**
     * Конфигурация спринг бина сохранения ЭП
     */
    @Element(name = "save-signature-bean-settings", required = false)
    @Convert(CollectorSettingsConverter.class)
    private CollectorSettings saveSignatureBeanSettings;    
    
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

    public String getSaveSignatureBeanName() {
        return saveSignatureBeanName;
    }

    public void setSaveSignatureBeanName(String saveSignatureBeanName) {
        this.saveSignatureBeanName = saveSignatureBeanName;
    }

    public CollectorSettings getGetContentBeanSettings() {
        return getContentBeanSettings;
    }

    public void setGetContentBeanSettings(CollectorSettings getContentBeanSettings) {
        this.getContentBeanSettings = getContentBeanSettings;
    }

    public CollectorSettings getSaveSignatureBeanSettings() {
        return saveSignatureBeanSettings;
    }

    public void setSaveSignatureBeanSettings(CollectorSettings saveSignatureBeanSettings) {
        this.saveSignatureBeanSettings = saveSignatureBeanSettings;
    }

    
}
