package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy Orlov on 15.08.2016.
 */
public class ExtendedSearchShowDialogBoxEvent extends GwtEvent<ExtendedSearchShowDialogBoxEventHandler> {

    public static GwtEvent.Type<ExtendedSearchShowDialogBoxEventHandler> TYPE = new GwtEvent.Type<ExtendedSearchShowDialogBoxEventHandler>();

    private boolean newDialog;
    private Map<String, WidgetState> extendedSearchConfiguration;
    private List<String> searchAreas;
    private String searchDomainObjectType;


    public ExtendedSearchShowDialogBoxEvent() {
        this(false, null, null, "");
    }

    public boolean isNewDialog() {
        return newDialog;
    }

    public void setNewDialog(boolean newDialog) {
        this.newDialog = newDialog;
    }

    public ExtendedSearchShowDialogBoxEvent(boolean newDialog, Map<String, WidgetState> extendedSearchConfiguration, List<String> searchArea, String searchDomainObjectType) {
        this.newDialog = newDialog;
        this.extendedSearchConfiguration = extendedSearchConfiguration;
        this.searchAreas = searchArea;
        this.searchDomainObjectType = searchDomainObjectType;
    }

    public List<String> getSearchAreas() {
        return searchAreas;
    }

    public void setSearchAreas(List<String> searchArea) {
        this.searchAreas = searchArea;
    }

    public String getSearchDomainObjectType() {
        return searchDomainObjectType;
    }

    public void setSearchDomainObjectType(String searchDomainObjectType) {
        this.searchDomainObjectType = searchDomainObjectType;
    }

    public Map<String, WidgetState> getExtendedSearchConfiguration() {
        return extendedSearchConfiguration;
    }

    public void setExtendedSearchConfiguration(Map<String, WidgetState> extendedSearchConfiguration) {
        this.extendedSearchConfiguration = extendedSearchConfiguration;
    }

    @Override
    public GwtEvent.Type<ExtendedSearchShowDialogBoxEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(ExtendedSearchShowDialogBoxEventHandler handler) {
        handler.onShowExtendedSearchDialog(this);
    }

}
