package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy Orlov on 12.08.2016.
 */
public class ExtendedSearchDomainObjectSurfacePluginData extends DomainObjectSurferPluginData {


    private Map<String, WidgetState> extendedSearchConfiguration= new HashMap<>();
    private List<String> searchAreas;
    private String searchDomainObjectType;

    public Map<String, WidgetState> getExtendedSearchConfiguration() {
        return extendedSearchConfiguration;
    }

    public void setExtendedSearchConfiguration(Map<String, WidgetState> extendedSearchConfiguration) {
        this.extendedSearchConfiguration = extendedSearchConfiguration;
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
}
