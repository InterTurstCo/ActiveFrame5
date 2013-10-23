package ru.intertrust.cm.core.config.model.gui;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.*;

/** 
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:02
 */
public class ActionConfig implements Dto {
    private String name;
    private String component;
    private String text;
    private String imageUrl;
    private boolean showText;
    private ActionSettingsConfig ActionSettingsConfig;

    public ActionConfig() {
    }

    public ActionConfig(String name) {
        this.name = name;
    }

    public ActionConfig(String name, String component) {
        this.name = name;
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean displayText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public ActionSettingsConfig getActionSettingsConfig() {
        return ActionSettingsConfig;
    }

    public void setActionSettingsConfig(ActionSettingsConfig actionSettingsConfig) {
        ActionSettingsConfig = actionSettingsConfig;
    }

    @Override
    public String toString() {
        return "ActionConfig {" +
                "name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", showText=" + showText +
                '}';
    }
}
