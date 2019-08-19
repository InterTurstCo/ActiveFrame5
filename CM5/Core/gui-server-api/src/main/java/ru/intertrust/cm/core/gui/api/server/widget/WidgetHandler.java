package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:58
 */
public abstract class WidgetHandler implements ComponentHandler {

    @Autowired
    protected ConfigurationExplorer configurationService;

    @Autowired
    protected FormatHandler formatHandler;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private ProfileService profileService;

    public abstract <T extends WidgetState> T getInitialState(WidgetContext context);

    public abstract Value getValue(WidgetState state);

    public void afterFormSave(FormState formState, WidgetConfig widgetConfig) {
    }

    public void beforeFormDelete() {
    }

    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        return null;
    }

    protected ArrayList<String> format(List<DomainObject> listToDisplay, String displayPattern, FormattingConfig formattingConfig) {
        Pattern pattern = Pattern.compile(FormatHandler.FIELD_PLACEHOLDER_PATTERN);
        Matcher matcher = pattern.matcher(displayPattern);
        ArrayList<String> displayValues = new ArrayList<>(listToDisplay.size());
        for (DomainObject domainObject : listToDisplay) {
            displayValues.add(formatHandler.format(domainObject, matcher, formattingConfig));
        }
        return displayValues;
    }

    protected void appendDisplayMappings(List<DomainObject> listToDisplay, String displayPattern,
                                         Map<Id, String> idDisplayMapping) {
        ArrayList<String> displayValues = format(listToDisplay, displayPattern, null);
        for (int i = 0; i < listToDisplay.size(); i++) {
            DomainObject domainObject = listToDisplay.get(i);
            idDisplayMapping.put(domainObject.getId(), displayValues.get(i));
        }
    }

    protected boolean isSingleChoice(WidgetContext context, Boolean singleChoiceFromConfig) {
       FieldPath[] fieldPaths = context.getFieldPaths();
       Boolean singleChoiceAnalyzed = null;
       for (FieldPath fieldPath : fieldPaths) {
           if (singleChoiceAnalyzed != null) {
              if (singleChoiceAnalyzed != (fieldPath.isOneToOneDirectReference() || fieldPath.isField())){
                  throw new GuiException(MessageResourceProvider.getMessage(
                          LocalizationKeys.GUI_EXCEPTION_MULTIPLE_FIELDPATHS,
                          "Multiply fieldPaths should be all reference type or all backreference type",
                          GuiContext.getUserLocale()));
              }
           }
            singleChoiceAnalyzed = fieldPath.isOneToOneDirectReference() || fieldPath.isField();
       }
       return singleChoiceAnalyzed || (singleChoiceFromConfig != null && singleChoiceFromConfig);
    }

    protected boolean isNullable(WidgetContext widgetContext) {
        FieldPath[] fieldPaths = widgetContext.getFieldPaths();
        if (fieldPaths.length > 1) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_SINGLE_FIELDPATH,
                    "Only single field-path is supported",
                    GuiContext.getUserLocale()));
        }
        if (fieldPaths[0].isField() || fieldPaths[0].isOneToOneDirectReference()) {
            return !configurationService.getFieldConfig(widgetContext.getFormObjects().getRootDomainObjectType(), fieldPaths[0].getFieldName()).isNotNull();
        }
        return true;
    }

    protected FieldConfig getFieldConfig(WidgetContext context) {
        final FieldPath fieldPath = context.getFirstFieldPath();
        String parentType = context.getFormObjects().getParentNode(fieldPath).getType();
        return configurationService.getFieldConfig(parentType, fieldPath.getFieldName());
    }

}
