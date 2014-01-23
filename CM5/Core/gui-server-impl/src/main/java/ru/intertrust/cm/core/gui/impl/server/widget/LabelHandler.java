package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends SingleObjectWidgetHandler {

    @Autowired
    protected ConfigurationService configurationService;

    @Override
    public LabelState getInitialState(WidgetContext context) {
        FieldPath[] fieldPaths = context.getFieldPaths();
        LabelConfig labelConfig = context.getWidgetConfig();
        LabelState state = new LabelState();
        state.setFontSize(labelConfig.getFontSize());
        state.setFontStyle(labelConfig.getFontStyle());
        state.setFontWeight(labelConfig.getFontWeight());
        if (fieldPaths[0] != null) {
            String formattedString = format(labelConfig.getPattern(), fieldPaths, context);
            state.setLabel(formattedString);
            return state;
        } else {
            state.setLabel(labelConfig.getText());
            state.setRelatedToRequiredField(isRelatedFieldRequired(context, labelConfig));

            return state;
        }
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    private String format(String configPattern, FieldPath[] fieldPaths, WidgetContext context) {
        final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
        String displayPattern = configPattern == null ? buildDefaultPattern(fieldPaths) : configPattern;
        Pattern pattern = Pattern.compile("\\{[\\w.]+\\}");
        Matcher matcher = pattern.matcher(displayPattern);
        StringBuffer replacement = new StringBuffer();
        boolean allEmpty = true;
        while (matcher.find()) {
            String group = matcher.group();
            FieldPath fieldPath = new FieldPath(group.substring(1, group.length() - 1));
            Value value = context.getValue(fieldPath);
            String displayValue = "";
            if (value != null) {
                Object primitiveValue = value.get();
                if (primitiveValue == null) {
                    if (value instanceof LongValue || value instanceof DecimalValue) {
                        displayValue = "0";
                    }
                } else {
                    allEmpty = false;
                    if (value instanceof TimestampValue) {
                        displayValue = DATE_FORMATTER.format(primitiveValue);
                    } else {
                        displayValue = primitiveValue.toString();
                    }
                }
            }
            matcher.appendReplacement(replacement, displayValue);
        }
        if (allEmpty) {
            return "";
        }

        matcher.appendTail(replacement);
        matcher.reset();
        return replacement.toString();
    }

    private String buildDefaultPattern(FieldPath[] fieldPaths) {
        StringBuilder pattern = new StringBuilder(fieldPaths.length * 5);
        for (int i = 0; i < fieldPaths.length; ++i) {
            FieldPath fieldPath = fieldPaths[i];
            if (i != 0) {
                pattern.append(", ");
            }
            pattern.append('{').append(fieldPath.getPath()).append('}');
        }
        return pattern.toString();
    }

    private boolean isRelatedFieldRequired(WidgetContext context, LabelConfig labelConfig) {
        if (labelConfig.getRelatesTo() != null) {
            String relatedWidgetId = labelConfig.getRelatesTo().getWidgetId();
            WidgetConfig relatedConfig = context.getWidgetConfigById(relatedWidgetId);
            FieldPath relatedFieldPath = new FieldPath(relatedConfig.getFieldPathConfig().getValue());
            String field = relatedFieldPath.getFieldName();
            String objectType = context.getFormObjects().getNode(relatedFieldPath.getParentPath()).getType();
            FieldConfig fieldConfig = configurationService.getFieldConfig(objectType, field);
            if (fieldConfig.isNotNull()) {
                return true;
            }
        }
        return false;
    }
}
