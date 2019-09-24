package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.navigation.counters.CounterType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "link")
public class  LinkConfig implements Dto,TopLevelConfig {

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "display-text", required = false)
    @Localizable
    private String displayText;

    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "display-counter", required = false)
    private boolean displayCounter = true;

    @Attribute(name = "counter-type", required = false)
    private String counterType;

    @Attribute(name = "child-to-open", required = false)
    private String childToOpen;

    @Attribute(name = "ref-link-name", required = false)
    private String refLinkName;

    @Attribute(name = "auto-cut", required = false)
    private Boolean autoCut;

    @Attribute(name = "tooltip", required = false)
    @Localizable
    private String tooltip;

    @Element(name = "plugin", required = false)
    private LinkPluginDefinition pluginDefinition;

    // Области поиска для расширенного поиска
    @Element(name = "default-search-areas", required = false)
    private DefaultSearchAreasConfig defaultSearchAreasConfig;

    @ElementList(inline = true, required = false)
    private List<ChildLinksConfig> childLinksConfigList = new ArrayList<ChildLinksConfig>();

    @Element(name = "decorations", required = false)
    private DecorationsConfig decorationsConfig;

    @Element(name = "outer", required = false)
    private OuterTypeConfig outerTypeConfig;

    @Transient
    private transient LinkConfig parentLinkConfig;
    @Transient
    private transient ChildLinksConfig parentChildLinksConfig;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getImage() {
        return image;
    }

    public String getChildToOpen() {
        return childToOpen;
    }

    public void setChildToOpen(String childToOpen) {
        this.childToOpen = childToOpen;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LinkPluginDefinition getPluginDefinition() {
        return pluginDefinition;
    }

    public void setPluginDefinition(LinkPluginDefinition pluginDefinition) {
        this.pluginDefinition = pluginDefinition;
    }

    public DefaultSearchAreasConfig getDefaultSearchAreasConfig() {
        return defaultSearchAreasConfig;
    }

    public void setDefaultSearchAreasConfig(DefaultSearchAreasConfig defaultSearchAreasConfig) {
        this.defaultSearchAreasConfig = defaultSearchAreasConfig;
    }

    public OuterTypeConfig getOuterTypeConfig() {
        return outerTypeConfig;
    }

    public void setOuterTypeConfig(OuterTypeConfig outerTypeConfig) {
        this.outerTypeConfig = outerTypeConfig;
    }

    public List<ChildLinksConfig> getChildLinksConfigList() {
        return childLinksConfigList;
    }

    public void setChildLinksConfigList(List<ChildLinksConfig> childLinksConfigList) {
        this.childLinksConfigList = childLinksConfigList;
    }

    public DecorationsConfig getDecorationsConfig() {
        return decorationsConfig;
    }

    public void setDecorationsConfig(DecorationsConfig decorationsConfig) {
        this.decorationsConfig = decorationsConfig;
    }

    public boolean isDisplayCounter() {
        return displayCounter;
    }

    public void setDisplayCounter(boolean displayCounter) {
        this.displayCounter = displayCounter;
    }

    public String getCounterType() {
        return counterType == null ? CounterType.ALL.value() : counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    public Boolean isAutoCut() {
        return autoCut;
    }

    public void setAutoCut(Boolean autoCut) {
        this.autoCut = autoCut;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getRefLinkName() {
        return refLinkName;
    }

    public void setRefLinkName(String refLinkName) {
        this.refLinkName = refLinkName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkConfig that = (LinkConfig) o;

        if (displayCounter != that.displayCounter) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (displayText != null ? !displayText.equals(that.displayText) : that.displayText != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (counterType != null ? !counterType.equals(that.counterType) : that.counterType != null) return false;
        if (childToOpen != null ? !childToOpen.equals(that.childToOpen) : that.childToOpen != null) return false;
        if (autoCut != null ? !autoCut.equals(that.autoCut) : that.autoCut != null) return false;
        if (tooltip != null ? !tooltip.equals(that.tooltip) : that.tooltip != null) return false;
        if (refLinkName != null ? !refLinkName.equals(that.refLinkName) : that.refLinkName != null) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (pluginDefinition != null ? !pluginDefinition.equals(that.pluginDefinition) : that.pluginDefinition != null)
            return false;
        if (defaultSearchAreasConfig != null ? !defaultSearchAreasConfig.equals(that.defaultSearchAreasConfig) : that.defaultSearchAreasConfig != null)
            return false;
        if (childLinksConfigList != null ? !childLinksConfigList.equals(that.childLinksConfigList) : that.childLinksConfigList != null)
            return false;
        if (decorationsConfig != null ? !decorationsConfig.equals(that.decorationsConfig) : that.decorationsConfig != null)
            return false;
        if (outerTypeConfig != null ? !outerTypeConfig.equals(that.outerTypeConfig) : that.outerTypeConfig != null)
            return false;
        if (parentLinkConfig != null ? !parentLinkConfig.equals(that.parentLinkConfig) : that.parentLinkConfig != null)
            return false;
        if (parentChildLinksConfig != null ? !parentChildLinksConfig.equals(that.parentChildLinksConfig) : that.parentChildLinksConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(LinkConfig.class.getSimpleName())
                .append(": name=").append(name)
                .append(", displayText=").append(displayText)
                .toString();
    }

    public LinkConfig getParentLinkConfig() {
        return parentLinkConfig;
    }

    public void setParentLinkConfig(LinkConfig parentLinkConfig) {
        this.parentLinkConfig = parentLinkConfig;
    }

    public ChildLinksConfig getParentChildLinksConfig() {
        return parentChildLinksConfig;
    }

    public void setParentChildLinksConfig(ChildLinksConfig parentChildLinksConfig) {
        this.parentChildLinksConfig = parentChildLinksConfig;
    }
}
