package ru.intertrust.cm.core.gui.impl.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ExtraParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.ExtraFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
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

    @Autowired
    private ProfileService profileService;

    public PluginData prepareHierarchicalCollectionData(Dto params) {
        ExpandHierarchicalCollectionData data = (ExpandHierarchicalCollectionData)params;
        ChildCollectionViewerConfig childCollectionViewerConfig = findChildCollectionViewerConfig(data);
        if (childCollectionViewerConfig == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_HIERARCH_COLLECTION,
                    profileService.getPersonLocale()));
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
        link.setDisplayText(childCollectionViewerConfig.getBreadCrumb());
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
        ExtraFilterConfig filterConfig = new ExtraFilterConfig();
        filterConfig.setName(filter);
        List<ExtraParamConfig> paramConfigs = new ArrayList<>();
        ExtraParamConfig paramConfig = new ExtraParamConfig();
        paramConfig.setName(0);
        paramConfig.setType(ModelConstants.REFERENCE_TYPE);
        paramConfig.setValue(selectedId.toStringRepresentation());
        paramConfigs.add(paramConfig);
        filterConfig.setParamConfigs(paramConfigs);
        List<ExtraFilterConfig> filterConfigs = new ArrayList<>();
        filterConfigs.add(filterConfig);
        CollectionExtraFiltersConfig filtersConfig = new CollectionExtraFiltersConfig();
        filtersConfig.setFilterConfigs(filterConfigs);
        collectionViewerConfig.setHierarchicalFiltersConfig(filtersConfig);
    }

    private String createLinkForHierarchicalCollection(ChildCollectionViewerConfig  childCollectionViewerConfig,
                                                       String currentCollectionName, Id parentId) {
        CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
        return currentCollectionName + "-" + parentId.toStringRepresentation() + "."
                + collectionViewerConfig.getCollectionRefConfig().getName();
    }
}
