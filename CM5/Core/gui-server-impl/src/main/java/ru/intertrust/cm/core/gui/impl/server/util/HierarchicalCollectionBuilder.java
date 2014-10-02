package ru.intertrust.cm.core.gui.impl.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 02.10.14
 *         Time: 19:53
 */

@ComponentName("hierarchical.collection.builder")
public class HierarchicalCollectionBuilder implements ComponentHandler {

    @Autowired
    private CrudService crudService;

    public PluginData prepareHierarchicalCollectionData(Dto params) {
        ExpandHierarchicalCollectionData data = (ExpandHierarchicalCollectionData)params;
        ChildCollectionViewerConfig childCollectionViewerConfig = findChildCollectionViewerConfig(data);
        if (childCollectionViewerConfig == null) {
            throw new GuiException("Ошибка в конфигурации иерархической коллекции");
        }
        CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
        collectionViewerConfig.setHierarchical(true);
        prepareFilterForHierarchicalCollection(collectionViewerConfig,
                childCollectionViewerConfig.getFilter(), data.getSelectedParentId());

        DomainObjectSurferConfig domainObjectSurferConfig = new DomainObjectSurferConfig();
        domainObjectSurferConfig.setCollectionViewerConfig(collectionViewerConfig);
        domainObjectSurferConfig.setDomainObjectTypeToCreate(childCollectionViewerConfig.getDomainObjectTypeToCreate());

        LinkConfig link = new LinkConfig();
        link.setName(createLinkForHierarchicalCollection(childCollectionViewerConfig, data.getCurrentCollectionName(),
                data.getSelectedParentId()));
        link.setDisplayText(childCollectionViewerConfig.getBreadCrumb() != null ?
                childCollectionViewerConfig.getBreadCrumb() : "Не определён");
        LinkPluginDefinition pluginDefinition = new LinkPluginDefinition();
        pluginDefinition.setPluginConfig(domainObjectSurferConfig);
        link.setPluginDefinition(pluginDefinition);

        HierarchicalCollectionData result = new HierarchicalCollectionData();
        result.setDomainObjectSurferConfig(domainObjectSurferConfig);
        result.setHierarchicalLink(link);
        return result;
    }

    private ChildCollectionViewerConfig findChildCollectionViewerConfig(ExpandHierarchicalCollectionData data ) {
        ChildCollectionViewerConfig defaultChildCollectionViewerConfig = null;
        String parentDomainObjectType = crudService.getDomainObjectType(data.getSelectedParentId());
        for (ChildCollectionViewerConfig childConfig : data.getChildCollectionViewerConfigs()) {
            if (parentDomainObjectType.equals(childConfig.getForDomainObjectType())) {
                return childConfig;
            }
            if (childConfig.getForDomainObjectType() == null) {
                defaultChildCollectionViewerConfig = childConfig;
            }
        }
        return defaultChildCollectionViewerConfig;
    }

    private void prepareFilterForHierarchicalCollection(
            CollectionViewerConfig collectionViewerConfig, String filter, Id selectedId) {
        AbstractFilterConfig filterConfig = new InitialFilterConfig();
        filterConfig.setName(filter);
        List<ParamConfig> paramConfigs = new ArrayList<>();
        ParamConfig paramConfig = new ParamConfig();
        paramConfig.setName(0);
        paramConfig.setType(ModelConstants.REFERENCE_TYPE);
        paramConfig.setValue(selectedId.toStringRepresentation());
        paramConfigs.add(paramConfig);
        filterConfig.setParamConfigs(paramConfigs);
        List<AbstractFilterConfig> abstractFilterConfigs = new ArrayList<>();
        abstractFilterConfigs.add(filterConfig);
        InitialFiltersConfig filtersConfig = new InitialFiltersConfig();
        filtersConfig.setAbstractFilterConfigs(abstractFilterConfigs);
        collectionViewerConfig.setHierarchicalFiltersConfig(filtersConfig);
    }

    private String createLinkForHierarchicalCollection(ChildCollectionViewerConfig  childCollectionViewerConfig,
                                                       String currentCollectionName, Id parentId) {
        CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
        return currentCollectionName + "-" + parentId.toStringRepresentation() + "."
                + collectionViewerConfig.getCollectionRefConfig().getName();
    }
}
