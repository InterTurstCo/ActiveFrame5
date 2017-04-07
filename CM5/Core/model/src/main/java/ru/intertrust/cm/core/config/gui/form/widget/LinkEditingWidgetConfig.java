package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.hierarchybrowser.CreateNewButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:34
 */
//TODO make higher abstraction
public abstract class LinkEditingWidgetConfig extends WidgetConfig implements HasLinkedFormMappings {

    @Element(name = "create-new-button",required = false)
    protected CreateNewButtonConfig createNewButtonConfig;

    @Element(name = "created-objects",required = false)
    protected CreatedObjectsConfig createdObjectsConfig;

    @Element(name = "linked-form-mapping",required = false)
    private LinkedFormMappingConfig linkedFormMappingConfig;

    public CreateNewButtonConfig getCreateNewButtonConfig() {
        return createNewButtonConfig;
    }

    public void setCreateNewButtonConfig(CreateNewButtonConfig createNewButtonConfig) {
        this.createNewButtonConfig = createNewButtonConfig;
    }

    public CreatedObjectsConfig getCreatedObjectsConfig() {
        return createdObjectsConfig;
    }

    public void setCreatedObjectsConfig(CreatedObjectsConfig createdObjectsConfig) {
        this.createdObjectsConfig = createdObjectsConfig;
    }

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }

    public void setLinkedFormMappingConfig(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
    }

    public abstract SelectionStyleConfig getSelectionStyleConfig();
    public abstract SelectionFiltersConfig getSelectionFiltersConfig();
    public abstract CollectionRefConfig getCollectionRefConfig();
    public abstract FormattingConfig getFormattingConfig();
    @Deprecated
    public abstract DefaultSortCriteriaConfig getDefaultSortCriteriaConfig();
    public abstract SelectionPatternConfig getSelectionPatternConfig();
    public abstract SelectionSortCriteriaConfig getSelectionSortCriteriaConfig();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) o;

        if (createNewButtonConfig != null ? !createNewButtonConfig.equals(config.createNewButtonConfig)
                : config.createNewButtonConfig != null) {
            return false;
        }
        if (createdObjectsConfig != null ? !createdObjectsConfig.equals(config.createdObjectsConfig)
                : config.createdObjectsConfig != null){
            return false;
        }
        if (linkedFormMappingConfig != null ? !linkedFormMappingConfig.equals(config.linkedFormMappingConfig)
                : config.linkedFormMappingConfig != null){
            return false;
        }

        return true;
    }
}
