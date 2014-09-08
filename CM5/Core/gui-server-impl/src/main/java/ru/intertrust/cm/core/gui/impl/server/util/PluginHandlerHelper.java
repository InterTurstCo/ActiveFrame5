package ru.intertrust.cm.core.gui.impl.server.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.util.ObjectCloner;

/**
 * @author Sergey.Okolot
 *         Created on 06.06.2014 12:00.
 */
public class PluginHandlerHelper {

    private PluginHandlerHelper() {}

    public static ActionConfig createActionConfig(final String name, final String component,
                                                  final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
    }

    public static ActionConfig cloneActionConfig(final ActionConfig config) {
        final ObjectCloner cloner = new ObjectCloner();
        final ActionConfig result = cloner.cloneObject(config, ActionConfig.class);
        return result;
    }

    public static void fillActionConfigFromRefConfig(final ActionConfig target, final ActionRefConfig source) {
        if (!source.isShowText()) {
            target.setText(null);
        }
        if (!source.isShowImage()) {
            target.setImageUrl(null);
        }
        if (source.getOrder() < Integer.MAX_VALUE) {
            target.setOrder(source.getOrder());
        }
        if (source.getRendered() != null) {
            target.setRendered(source.getRendered());
        }
        if (source.getMerged() != null) {
            target.setMerged(source.getMerged());
        }
        target.setVisibleWhenNew(source.isVisibleWhenNew());
        if (source.getVisibilityStateCondition() != null) {
            target.setVisibilityStateCondition(source.getVisibilityStateCondition());
        }
        if (source.getVisibilityChecker() != null) {
            target.setVisibilityChecker(source.getVisibilityChecker());
        }
        target.getProperties().putAll(source.getProperties());
    }

    public static <T extends Dto> T deserializeFromXml(Class<T> type, String asStr) {
        final Serializer serializer = new Persister();
        try {
            T result = serializer.read(type, asStr);
            return result;
        } catch (Exception ignored) {}
        return null;
    }

    public static <T extends Dto> String serializeToXml(T source) {
        final Serializer serializer = new Persister();
        final StringWriter writer = new StringWriter();
        try {
            serializer.write(source, writer);
            return writer.toString();
        } catch (Exception ignored) {}
        return null;
    }

    public static IdentifiableObject getUserSettingsIdentifiableObject(final String userLogin,
                                                                       final CollectionsService collectionsService) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byPerson", 0, new StringValue(userLogin)));
        final IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_user_settings_collection", null, filters);
        return collection.size() == 0 ? null : collection.get(0);
    }

    public static DomainObject getUserSettingsDomainObject(final CurrentUserAccessor currentUserAccessor,
                                                           final CollectionsService collectionsService,
                                                           final CrudService crudService) {
        final IdentifiableObject identifiableObject =
                getUserSettingsIdentifiableObject(currentUserAccessor.getCurrentUser(), collectionsService);
        final DomainObject result;
        if (identifiableObject != null) {
            result = crudService.find(identifiableObject.getId());
        } else {
            result = crudService.createDomainObject("bu_user_settings");
            result.setReference("person", currentUserAccessor.getCurrentUserId());
        }
        return result;
    }

    public static IdentifiableObject getCollectionSettingIdentifiableObject(final String link,
                                                                            final String viewName,
                                                                            final String userLogin,
                                                                            final CollectionsService collectionsService) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byLink", 0, new StringValue(link)));
        filters.add(Filter.create("byCollectionViewName", 0, new StringValue(viewName)));
        filters.add(Filter.create("byPerson", 0, new StringValue(userLogin)));
        final IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_nav_link_collections", null, filters);
        return collection.size() == 0 ? null : collection.get(0);
    }

    public static DomainObject getCollectionSettingsDomainObject(final String link, final String viewName,
                                           final CurrentUserAccessor currentUserAccessor,
                                           final CrudService crudService,
                                           final CollectionsService collectionsService) {
        final IdentifiableObject identifiableObject = getCollectionSettingIdentifiableObject(link,
                viewName, currentUserAccessor.getCurrentUser(), collectionsService);
        final DomainObject result;
        if (identifiableObject == null) {
            result = crudService.createDomainObject("bu_nav_link_collection");
            result.setValue("link", new StringValue(link));
            result.setValue("collection_view_name", new StringValue(viewName));
            result.setValue("person", new ReferenceValue(currentUserAccessor.getCurrentUserId()));
        } else {
            result = crudService.find(identifiableObject.getId());
        }
        return result;
    }

    public static CollectionViewConfig findCollectionViewConfig(final String collectionName, String collectionViewName,
                                                                final String userLogin, final String link,
                                                                final ConfigurationService configurationService,
                                                                final CollectionsService collectionsService) {
        if (collectionViewName == null) {
            collectionViewName = findDefaultCollectionViewName(collectionName, configurationService);
        }
        final IdentifiableObject identifiableObject = getCollectionSettingIdentifiableObject(link,
                collectionViewName, userLogin, collectionsService);
        CollectionViewConfig result = null;
        if (identifiableObject != null) {
            result = deserializeFromXml(CollectionViewConfig.class,
                    identifiableObject.getString(UserSettingsHelper.DO_COLLECTION_VIEW_FIELD_KEY));
        }
        if (result == null) {
            result = configurationService.getConfig(CollectionViewConfig.class, collectionViewName);
        }
        if (result == null) {
            throw new UnexpectedException("Couldn't find collection view with name '" + collectionViewName + "'");
        }
        return result;
    }

    private static String findDefaultCollectionViewName(final String collectionName,
                                                        final ConfigurationService configurationService) {
        final Collection<CollectionViewConfig> collectionViewConfigs =
                configurationService.getConfigs(CollectionViewConfig.class);
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {
            boolean isDefault = collectionViewConfig.isDefault();
            if (collectionViewConfig.getCollection().equalsIgnoreCase(collectionName) && isDefault) {
                return collectionViewConfig.getName();
            }
        }
        throw new UnexpectedException("Couldn't find view for collection with name '" + collectionName + "'");
    }
}
