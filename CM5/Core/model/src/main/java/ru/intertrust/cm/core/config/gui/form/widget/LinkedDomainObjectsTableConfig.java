package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "linked-domain-objects-table")
public class LinkedDomainObjectsTableConfig extends WidgetConfig implements HasLinkedFormMappings {

    @Attribute(name = "modal-width",required = false)
    private String modalWidth;
    @Attribute(name = "modal-height",required = false)
    private String modalHeight;
    @Attribute(name = "delete-linked-objects",required = false)
    private Boolean deleteLinkedObjects;

    @Element(name = "linked-form", required = false)
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "linked-form-mapping",required = false)
    private LinkedFormMappingConfig linkedFormMappingConfig;

    @Element(name = "created-objects",required = false)
    private CreatedObjectsConfig createdObjectsConfig;

    @Element(name = "pattern", required = false)
    private PatternConfig patternConfig;

    @NotNullLogicalValidation
    @Element(name = "summary-table", required = false)
    private SummaryTableConfig summaryTableConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoiceConfig;

    @Element(name = "selection-filters", required = false)
    private SelectionFiltersConfig selectionFiltersConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "selection-sort-criteria",required = false)
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

    public SummaryTableConfig getSummaryTableConfig() {
        return summaryTableConfig;
    }

    public void setSummaryTableConfig(SummaryTableConfig summaryTableConfig) {
        this.summaryTableConfig = summaryTableConfig;
    }
    public String getModalWidth() {
        return modalWidth;
    }

    public void setModalWidth(String modalWidth) {
        this.modalWidth = modalWidth;
    }

    public String getModalHeight() {
        return modalHeight;
    }

    public Boolean isDeleteLinkedObjects() {
        return deleteLinkedObjects;
    }

    public void setDeleteLinkedObjects(Boolean deleteLinkedObjects) {
        this.deleteLinkedObjects = deleteLinkedObjects;
    }

    public void setModalHeight(String modalHeight) {
        this.modalHeight = modalHeight;
    }

    public SingleChoiceConfig getSingleChoiceConfig() {
        return singleChoiceConfig;
    }

    public void setSingleChoiceConfig(SingleChoiceConfig singleChoiceConfig) {
        this.singleChoiceConfig = singleChoiceConfig;
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

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }

    public void setLinkedFormMappingConfig(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
    }

    public CreatedObjectsConfig getCreatedObjectsConfig() {
        return createdObjectsConfig;
    }

    public void setCreatedObjectsConfig(CreatedObjectsConfig createdObjectsConfig) {
        this.createdObjectsConfig = createdObjectsConfig;
    }

    @Override
    public String getComponentName() {
        return "linked-domain-objects-table";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LinkedDomainObjectsTableConfig that = (LinkedDomainObjectsTableConfig) o;

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that.collectionRefConfig != null)
            return false;
        if (createdObjectsConfig != null ? !createdObjectsConfig.equals(that.createdObjectsConfig) : that.createdObjectsConfig != null)
            return false;
        if (deleteLinkedObjects != null ? !deleteLinkedObjects.equals(that.deleteLinkedObjects) : that.deleteLinkedObjects != null)
            return false;
        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null)
            return false;
        if (linkedFormMappingConfig != null ? !linkedFormMappingConfig.equals(that.linkedFormMappingConfig) : that.linkedFormMappingConfig != null)
            return false;
        if (modalHeight != null ? !modalHeight.equals(that.modalHeight) : that.modalHeight != null) return false;
        if (modalWidth != null ? !modalWidth.equals(that.modalWidth) : that.modalWidth != null) return false;
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null)
            return false;
        if (selectionFiltersConfig != null ? !selectionFiltersConfig.equals(that.selectionFiltersConfig) : that.selectionFiltersConfig != null)
            return false;
        if (selectionSortCriteriaConfig != null ? !selectionSortCriteriaConfig.equals(that.selectionSortCriteriaConfig) : that.selectionSortCriteriaConfig != null)
            return false;
        if (singleChoiceConfig != null ? !singleChoiceConfig.equals(that.singleChoiceConfig) : that.singleChoiceConfig != null)
            return false;
        if (summaryTableConfig != null ? !summaryTableConfig.equals(that.summaryTableConfig) : that.summaryTableConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (modalWidth != null ? modalWidth.hashCode() : 0);
        result = 31 * result + (modalHeight != null ? modalHeight.hashCode() : 0);
        result = 31 * result + (deleteLinkedObjects != null ? deleteLinkedObjects.hashCode() : 0);
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (linkedFormMappingConfig != null ? linkedFormMappingConfig.hashCode() : 0);
        result = 31 * result + (createdObjectsConfig != null ? createdObjectsConfig.hashCode() : 0);
        result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
        result = 31 * result + (summaryTableConfig != null ? summaryTableConfig.hashCode() : 0);
        result = 31 * result + (singleChoiceConfig != null ? singleChoiceConfig.hashCode() : 0);
        result = 31 * result + (selectionFiltersConfig != null ? selectionFiltersConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (selectionSortCriteriaConfig != null ? selectionSortCriteriaConfig.hashCode() : 0);
        return result;
    }
}

