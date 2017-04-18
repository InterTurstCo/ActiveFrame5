package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkConfig extends LinkEditingWidgetConfig {

    @Attribute(name = "modal-window", required = false)
    private boolean modalWindow = true;

    @Element(name = "linked-form", required = false)
    private LinkedFormConfig linkedFormConfig;

    @NotNullLogicalValidation
    @Element(name = "pattern", required = false)
    private PatternConfig patternConfig;

    @Element(name = "selection-style", required = false)
    private SelectionStyleConfig selectionStyleConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    @Element(name = "selection-filters", required = false)
    private SelectionFiltersConfig selectionFiltersConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "selection-sort-criteria", required = false)
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;

    public LinkedFormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(LinkedFormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
    }

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    @Override
    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return null;
    }

    @Override
    public SelectionPatternConfig getSelectionPatternConfig() {
        SelectionPatternConfig selectionPatternConfig = new SelectionPatternConfig();
        selectionPatternConfig.setValue(patternConfig.getValue());
        return selectionPatternConfig;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public void setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public SelectionSortCriteriaConfig getSelectionSortCriteriaConfig() {
        return selectionSortCriteriaConfig;
    }

    public void setSelectionSortCriteriaConfig(SelectionSortCriteriaConfig selectionSortCriteriaConfig) {
        this.selectionSortCriteriaConfig = selectionSortCriteriaConfig;
    }

    public boolean isModalWindow() {
        return modalWindow;
    }

    public void setModalWindow(boolean modalWindow) {
        this.modalWindow = modalWindow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LinkedDomainObjectHyperlinkConfig that = (LinkedDomainObjectHyperlinkConfig) o;
        if (modalWindow != that.modalWindow) {
            return false;
        }
        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that.collectionRefConfig != null)
            return false;
        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null)
            return false;
        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null)
            return false;
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null)
            return false;
        if (selectionFiltersConfig != null ? !selectionFiltersConfig.equals(that.selectionFiltersConfig) : that.selectionFiltersConfig != null)
            return false;
        if (selectionSortCriteriaConfig != null ? !selectionSortCriteriaConfig.equals(that.selectionSortCriteriaConfig) : that.selectionSortCriteriaConfig != null)
            return false;
        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) : that.selectionStyleConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
        result = 31 * result + (selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0);
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        result = 31 * result + (selectionFiltersConfig != null ? selectionFiltersConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (selectionSortCriteriaConfig != null ? selectionSortCriteriaConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "linked-domain-object-hyperlink";
    }

    @Override
    public String getLogicalValidatorComponentName() {
        return "linkedDomainObjectHyperlinkLogicalValidator";
    }
}
