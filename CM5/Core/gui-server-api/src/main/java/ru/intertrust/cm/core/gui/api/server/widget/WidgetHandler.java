package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import javax.inject.Inject;
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

    @Inject
    protected ConfigurationService configurationService;
    @Autowired
    protected FormatHandler formatHandler;

    public abstract <T extends WidgetState> T getInitialState(WidgetContext context);

    public abstract Value getValue(WidgetState state);

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

    protected boolean isSingleChoice(WidgetContext context, boolean singleChoiceFromConfig) {
       FieldPath[] fieldPaths = context.getFieldPaths();
       Boolean singleChoiceAnalyzed = null;
       for (FieldPath fieldPath : fieldPaths) {
           if (singleChoiceAnalyzed != null) {
              if (singleChoiceAnalyzed != (fieldPath.isOneToOneDirectReference() || fieldPath.isField())){
                  throw new GuiException("Multiply fieldPaths should be all reference type or all backreference type");
              }
           }
            singleChoiceAnalyzed = fieldPath.isOneToOneDirectReference() || fieldPath.isField();
       }
       return singleChoiceAnalyzed || singleChoiceFromConfig;
    }

    protected boolean isNullable(WidgetContext widgetContext) {
        FieldPath[] fieldPaths = widgetContext.getFieldPaths();
        if (fieldPaths.length > 1) {
            throw new GuiException("Only single field-path is supported");
        }
        if (fieldPaths[0].isField() || fieldPaths[0].isOneToOneDirectReference()) {
            return !configurationService.getFieldConfig(widgetContext.getFormObjects().getRootDomainObjectType(), fieldPaths[0].getFieldName()).isNotNull();
        }
        return true;
    }
}
