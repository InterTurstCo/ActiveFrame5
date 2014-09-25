package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginState;
import ru.intertrust.cm.core.gui.model.plugin.ExpandHierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.HierarchicalCollectionData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_SPLITTER_ORIENTATION_FIELD_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DO_SPLITTER_POSITION_FIELD_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SELECTED_IDS_KEY;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private CrudService crudService;

    public ActivePluginData initialize(Dto params) {
        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) params;
        config.getCollectionViewerConfig().addHistoryValues(config.getHistoryValues());

        final CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");
        final CollectionPluginData collectionPluginData = collectionPluginHandler.initialize(config.getCollectionViewerConfig());
        final ArrayList<CollectionRowItem> items = collectionPluginData.getItems();
        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
        } else {
            final List<Id> selectedIds = new ArrayList<>();
            final List<Id> selectedFromHistory = config.getHistoryValue(SELECTED_IDS_KEY);
            if (selectedFromHistory != null && !selectedFromHistory.isEmpty()) {
                selectedIds.addAll((List) config.getHistoryValue(SELECTED_IDS_KEY));
            } else {
                selectedIds.add(items.get(0).getId());
            }
            formPluginConfig = new FormPluginConfig(selectedIds.get(0));
            collectionPluginData.setChosenIds(selectedIds);
        }
        formPluginConfig.addHistoryValues(config.getHistoryValues());
        final FormPluginState fpState = new FormPluginState();
        formPluginConfig.setPluginState(fpState);
        formPluginConfig.setFormViewerConfig(config.getFormViewerConfig());
        fpState.setToggleEdit(config.isToggleEdit());
        fpState.setEditable(!config.isToggleEdit());
        final FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        final DomainObjectSurferPluginData result = new DomainObjectSurferPluginData();
        result.setCollectionPluginData(collectionPluginData);
        result.setFormPluginData(formPluginData);
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(config.isToggleEdit());
        result.setPluginState(dosState);
        final IdentifiableObject identifiableObject = PluginHandlerHelper.getUserSettingsIdentifiableObject(
                currentUserAccessor.getCurrentUser(), collectionsService);
        if (identifiableObject != null) {
            final Long splitterOrientation = identifiableObject.getLong(DO_SPLITTER_ORIENTATION_FIELD_KEY);
            final Long splitterPosition = identifiableObject.getLong(DO_SPLITTER_POSITION_FIELD_KEY);
            if (splitterOrientation != null && splitterPosition != null) {
                result.setSplitterOrientation(splitterOrientation.intValue());
                result.setSplitterPosition(splitterPosition.intValue());
            }
        }
        return result;
    }

    //TODO: [CMFIVE-451] move this code out from the handler + rename.
    public PluginData initializeForHierarchicalCollection(Dto params) {
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
        AbstractFiltersConfig filtersConfig = new SelectionFiltersConfig();
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
