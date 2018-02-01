package ru.intertrust.cm.core.gui.impl.server.util;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.action.system.SettingsUtil;
import ru.intertrust.cm.core.gui.impl.server.validation.CustomValidatorFactory;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.*;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.gui.model.validation.ValidationMessage;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.model.FatalException;

import javax.ejb.EJB;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 06.06.2014 12:00.
 */
public class PluginHandlerHelper {


    public static final String DOMAIN_OBJECT_KEY = "domainObject";
    public static final String WORKFLOW_PROCESS_TYPE_KEY = "action.type";
    public static final String WORKFLOW_PROCESS_NAME_KEY = "process.name";

    private PluginHandlerHelper() {}

    public static ActionConfig createActionConfig(final String name, final String component,
                                                  final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
    }

    public static ActionConfig cloneActionConfig(final ActionConfig config) {
        final ActionConfig result = ObjectCloner.getInstance().cloneObject(config, config.getClass());
        return result;
    }

    public static void fillActionConfigFromRefConfig(final ActionConfig target, final ActionRefConfig source) {
        if (source.getText() != null) {
            target.setText(source.getText());
        }
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
        if (source.getPermissions() != null) {
            target.setPermissions(source.getPermissions());
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

    public static DomainObject findAndLockUserSettingsDomainObject(final CurrentUserAccessor currentUserAccessor,
                                                                   final CollectionsService collectionsService,
                                                                   final CrudService crudService) {
        final IdentifiableObject identifiableObject =
                getUserSettingsIdentifiableObject(currentUserAccessor.getCurrentUser(), collectionsService);
        final DomainObject result;
        if (identifiableObject != null) {
            result = crudService.findAndLock(identifiableObject.getId());
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

    public static DomainObject findAndLockCollectionSettingsDomainObject(final String link, final String viewName,
                                                                         final CurrentUserAccessor currentUserAccessor,
                                                                         final CrudService crudService,
                                                                         final CollectionsService collectionsService,
                                                                         final SettingsUtil settingsUtil) {
        final IdentifiableObject identifiableObject = getCollectionSettingIdentifiableObject(link,
                viewName, currentUserAccessor.getCurrentUser(), collectionsService);
        final DomainObject result;
        if (identifiableObject == null) {
            result = settingsUtil.createNewObject(link,currentUserAccessor.getCurrentUserId(),0L,viewName);
        } else {
            result = crudService.findAndLock(identifiableObject.getId());
        }
        return result;
    }

    public static CollectionViewConfig findCollectionViewConfig(final String collectionName, String collectionViewName,
                                                                final String userLogin, final String link,
                                                                final ConfigurationExplorer configurationService,
                                                                final CollectionsService collectionsService,
                                                                String locale) {
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
            result = configurationService.getLocalizedConfig(CollectionViewConfig.class, collectionViewName, locale);
        }
        if (result == null) {
            throw new FatalException("Couldn't find collection view with name '" + collectionViewName + "'");
        }
        return result;
    }

    private static String findDefaultCollectionViewName(final String collectionName,
                                                        final ConfigurationExplorer configurationService) {
        final Collection<CollectionViewConfig> collectionViewConfigs =
                configurationService.getConfigs(CollectionViewConfig.class);
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {
            boolean isDefault = collectionViewConfig.isDefault();
            if (collectionViewConfig.getCollection().equalsIgnoreCase(collectionName) && isDefault) {
                return collectionViewConfig.getName();
            }
        }
        throw new FatalException("Couldn't find view for collection with name '" + collectionName + "'");
    }

    public static List<String> doServerSideValidation(final FormState formState,
                                                      final ApplicationContext applicationContext, String locale) {
        //Simple Server Validation
        ConfigurationExplorer explorer = (ConfigurationExplorer) applicationContext.getBean("configurationExplorer");
        FormConfig formConfig = explorer.getPlainFormConfig(formState.getName());
        final CaseInsensitiveHashMap<WidgetConfig> widgetConfigsById = formConfig.getWidgetConfigsById();
        List<Constraint> constraints = new ArrayList<>();
        for (WidgetState state : formState.getFullWidgetsState().values()) {
            constraints.addAll(state.getConstraints());
        }
        List<String> errorMessages = new ArrayList<String>();
        for (Constraint constraint : constraints) {
            Value valueToValidate = getValueToValidate(constraint, formState, widgetConfigsById, applicationContext);
            ServerValidator validator = createValidator(constraint);
            if (validator != null) {
                validator.init(formState);
                ValidationResult validationResult = validator.validate(valueToValidate);
                if (validationResult.hasErrors()) {
                    errorMessages.addAll(getMessages(validationResult, constraint.getParams(), locale));
                }
            }
        }
        return errorMessages;
    }

    public static List<String> doCustomServerSideValidation(FormState formState, List<ValidatorConfig> validatorConfigs, String locale) {
        List<String> errorMessages = new ArrayList<>();
        if (validatorConfigs != null) {
            for (ValidatorConfig config : validatorConfigs) {
                String widgetId = config.getWidgetId();
                ServerValidator customValidator = CustomValidatorFactory.createInstance(config.getClassName(), widgetId);
                if (customValidator != null) {
                    WidgetState state = formState.getWidgetState(widgetId);
                    customValidator.init(formState);
                    ValidationResult validationResult = customValidator.validate(state);
                    if (validationResult.hasErrors()) {
                        errorMessages.addAll(getMessages(validationResult, null, locale));
                    }
                }
            }
        }
        return errorMessages;
    }

    private static Value getValueToValidate(Constraint constraint, FormState formState,
                                            CaseInsensitiveHashMap<WidgetConfig> widgetConfigsById, final ApplicationContext applicationContext) {
        String widgetId = constraint.param(Constraint.PARAM_WIDGET_ID);
        String componentName = formState.getWidgetComponent(widgetId);

        WidgetState state = formState.getWidgetState(widgetId);
        if (state != null && componentName != null) {
            WidgetHandler handler = getWidgetHandler(widgetConfigsById.get(widgetId), applicationContext);
            return handler.getValue(state);
        }
        return null;
    }

    public static WidgetHandler getWidgetHandler(WidgetConfig config, ApplicationContext applicationContext) {
        String handlerName = config.getHandler();
        if (handlerName == null || handlerName.isEmpty()) {
            handlerName = config.getComponentName();
        }
        return (WidgetHandler) applicationContext.getBean(handlerName);
    }

    private static ServerValidator createValidator(Constraint constraint) {
        switch (constraint.getType()) {
            case SIMPLE:
                return new SimpleValidator(constraint);
            case LENGTH:
                return new LengthValidator(constraint);
            case INT_RANGE:
                return new IntRangeValidator(constraint);
            case DECIMAL_RANGE:
                return new DecimalRangeValidator(constraint);
            case DATE_RANGE:
                return new DateRangeValidator(constraint);
            case SCALE_PRECISION:
                return new ScaleAndPrecisionValidator(constraint);
        }
        return null;
    }

    private static List<String> getMessages(ValidationResult validationResult,  Map<String, String> params, String locale) {
        List<String> messages = new ArrayList<String>();
        for (ValidationMessage msg : validationResult.getMessages()) {
            messages.add(getMessageText(msg.getMessage(), params, locale));
        }
        return messages;
    }

    private static String getMessageText(String messageKey, Map<String, String> props, String locale) {
        if (MessageResourceProvider.getMessages(locale).get(messageKey) != null) {
            return PlaceholderResolver.substitute(MessageResourceProvider.getMessage(messageKey, locale), props);
        } else {
            return messageKey;//let's return at least messageKey if the message is not found
        }
    }
}
