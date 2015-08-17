package ru.intertrust.cm.core.config.form.widget;

import ru.intertrust.cm.core.config.LogicalErrors;
import ru.intertrust.cm.core.config.WidgetConfigurationToValidate;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RendererConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.08.2015
 *         Time: 17:57
 */
public class LabelWidgetLogicalValidator extends AbstractWidgetLogicalValidator {

    @Override
    public void validate(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        validateAllowedCombinationOfTags(widget, logicalErrors);
    }

    private void validateAllowedCombinationOfTags(WidgetConfigurationToValidate widget, LogicalErrors logicalErrors) {
        LabelConfig labelConfig = (LabelConfig) widget.getWidgetConfig();
        String text = labelConfig.getText();
        PatternConfig patternConfig = labelConfig.getPattern();
        RendererConfig rendererConfig = labelConfig.getRenderer();
        if (text != null && patternConfig != null) {
            String error = String.format("Widget with id '%s' has redundant tag <pattern>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
        if (text != null && rendererConfig != null) {
            String error = String.format("Widget with id '%s' has redundant tag <renderer>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }
        if (text == null && rendererConfig != null && patternConfig != null) {
            String error = String.format("Widget with id '%s' has redundant tag <pattern>",
                    labelConfig.getId());
            logger.error(error);
            logicalErrors.addError(error);
        }

    }
}
