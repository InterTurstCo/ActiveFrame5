package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends ValueEditingWidgetHandler {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FormatHandler formatHandler;

    @Override
    public LabelState getInitialState(WidgetContext context) {
        FieldPath[] fieldPaths = context.getFieldPaths();
        LabelConfig labelConfig = context.getWidgetConfig();
        LabelState state = new LabelState();
        setStylesFromConfig(labelConfig, state);
        String textFromConfig = labelConfig.getText();
        if (textFromConfig != null) {
            state.setLabel(textFromConfig);
        }
        if (fieldPaths.length > 0 && fieldPaths[0] != null) {
            RendererConfig renderer = labelConfig.getRenderer();
            String rendererName = renderer == null ? null : renderer.getValue();
            if (rendererName != null){
                LabelRenderer customLabelRenderer = (LabelRenderer) applicationContext.getBean(rendererName);
                String composedText = customLabelRenderer.composeString(fieldPaths, context);
                state.setLabel(composedText);
            } else if (isFieldPathsEmpty(fieldPaths, context)) {
                final String emptyMessage = labelConfig.getAllValuesEmptyMessage() == null
                        ? null
                        : labelConfig.getAllValuesEmptyMessage().getValue();
                state.setLabel(emptyMessage == null ? "" : emptyMessage);
            } else {
                FormattingConfig formattingConfig = labelConfig.getFormattingConfig();
                String formattedString = format(labelConfig.getPattern(), fieldPaths, context,
                        formattingConfig);
                state.setLabel(formattedString);
            }
        }
        state.setAsteriskRequired(isAsteriskRequired(context, labelConfig));
        state.setRelatedWidgetId(labelConfig.getRelatesTo() != null ?  labelConfig.getRelatesTo().getWidgetId() : null);
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    private String format(PatternConfig configPattern, FieldPath[] fieldPaths, WidgetContext context,
                          FormattingConfig formattingConfig ) {
        String displayPattern = (configPattern == null || configPattern.getValue() == null)
                ? buildDefaultPattern(fieldPaths) : configPattern.getValue();
        Pattern pattern = Pattern.compile("\\{[\\w.]+\\}");
        Matcher matcher = pattern.matcher(displayPattern);
        String replacement = formatHandler.format(context, matcher, formattingConfig);
        return replacement;
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
    private boolean isAsteriskRequired(WidgetContext context, LabelConfig labelConfig) {
        return isForceRequiredAsterisk(labelConfig) || isRelatedFieldRequired(context, labelConfig);

    }

    private boolean isForceRequiredAsterisk(LabelConfig labelConfig) {
        ForceRequiredAsteriskConfig forceRequiredAsteriskConfig = labelConfig.getForceRequiredAsterisk();
        if(forceRequiredAsteriskConfig == null){
            return false;
        }
        return forceRequiredAsteriskConfig.isForceRequiredAsterisk();
    }

    private boolean isRelatedFieldRequired(WidgetContext context, LabelConfig labelConfig) {

        if (labelConfig.getRelatesTo() != null) {
            String relatedWidgetId = labelConfig.getRelatesTo().getWidgetId();
            WidgetConfig relatedConfig = context.getWidgetConfigById(relatedWidgetId);
            if (relatedConfig == null) {
                return false;
            }
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

    private void setStylesFromConfig(LabelConfig labelConfig, LabelState state) {
        FontStyleConfig fontStyleConfig = labelConfig.getFontStyle();
        FontWeightConfig fontWeightConfig = labelConfig.getFontWeight();
        FontSizeConfig fontSizeConfig = labelConfig.getFontSize();
        TextDecorationConfig textDecorationConfig = labelConfig.getTextDecorationConfig();
        BackgroundColorConfig backgroundColorConfig = labelConfig.getBackgroundColorConfig();
        String fontSize = fontSizeConfig == null ? null : fontSizeConfig.getValue();
        String fontStyle = fontStyleConfig == null ? null : fontStyleConfig.getValue();
        String fontWeight = fontWeightConfig == null ? null : fontWeightConfig.getValue();
        String textDecoration = textDecorationConfig == null ? null : textDecorationConfig.getValue();
        String backgroundColor = backgroundColorConfig == null ? null : backgroundColorConfig.getValue();
        state.setFontSize(fontSize);
        state.setFontStyle(fontStyle);
        state.setFontWeight(fontWeight);
        state.setTextDecoration(textDecoration);
        state.setBackgroundColor(backgroundColor);

    }

    private boolean isFieldPathsEmpty(final FieldPath[] fieldPaths, final WidgetContext context) {
        boolean result = true;
        for (FieldPath fieldPath : fieldPaths) {
            if (fieldPath != null) {
                if ("id".equals(fieldPath.getFieldName())) {
                    final Id id = context.getFormObjects().getRootDomainObject().getId();
                    result &= (id == null);
                } else {
                    result &= (context.getValue(fieldPath) == null);
                }
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }
}
