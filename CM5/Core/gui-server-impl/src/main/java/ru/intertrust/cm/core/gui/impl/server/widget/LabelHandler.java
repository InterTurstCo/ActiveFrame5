package ru.intertrust.cm.core.gui.impl.server.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AllValuesEmptyMessageConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FontSizeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FontStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FontWeightConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ForceRequiredAsteriskConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends ValueEditingWidgetHandler {

    @Autowired
    protected ConfigurationService configurationService;

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
        if (fieldPaths[0] != null) {
            RendererConfig renderer = labelConfig.getRenderer();
            String rendererName = renderer == null ? null : renderer.getValue();
            if (rendererName != null){
                LabelRenderer customLabelRenderer = (LabelRenderer) applicationContext.getBean(rendererName);
                String composedText = customLabelRenderer.composeString(fieldPaths, context);
                state.setLabel(composedText);
            } else {
                AllValuesEmptyMessageConfig allValuesEmptyMessage = labelConfig.getAllValuesEmptyMessage();
                String allValuesEmpty = allValuesEmptyMessage == null ? "" : allValuesEmptyMessage.getValue();
                FormattingConfig formattingConfig = labelConfig.getFormattingConfig();
                String formattedString = format(labelConfig.getPattern().getValue(), fieldPaths, context,
                        allValuesEmpty, formattingConfig);
                state.setLabel(formattedString);
            }

        }
        state.setAsteriskRequired(isAsteriskRequired(context, labelConfig));
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }

    private String format(String configPattern, FieldPath[] fieldPaths, WidgetContext context, String allValuesEmpty,
                          FormattingConfig formattingConfig ) {
        String displayPattern = configPattern == null ? buildDefaultPattern(fieldPaths) : configPattern;
        Pattern pattern = Pattern.compile("\\{[\\w.]+\\}");
        Matcher matcher = pattern.matcher(displayPattern);
        String replacement = formatHandler.format(context, matcher, allValuesEmpty, formattingConfig);
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
        String fontSize = fontSizeConfig == null ? null : fontSizeConfig.getValue();
        String fontStyle = fontStyleConfig == null ? null : fontStyleConfig.getValue();
        String fontWeight = fontWeightConfig == null ? null : fontWeightConfig.getValue();
        state.setFontSize(fontSize);
        state.setFontStyle(fontStyle);
        state.setFontWeight(fontWeight);

    }
}
