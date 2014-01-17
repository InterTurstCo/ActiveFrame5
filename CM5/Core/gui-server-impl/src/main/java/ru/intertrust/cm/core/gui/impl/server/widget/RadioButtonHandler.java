package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.config.gui.form.widget.RadioButtonConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.RadioButtonState;
import ru.intertrust.cm.core.gui.model.form.widget.SingleSelectionWidgetState;

/**
 * @author Lesia Puhova
 *         Date: 14.01.14
 *         Time: 18:29
 */
@ComponentName("radio-button")
public class RadioButtonHandler extends SingleSelectionWidgetHandler {

    @Override
    public SingleSelectionWidgetState getInitialState(WidgetContext context) {
        RadioButtonState result = new RadioButtonState();
        setupInitialState(result, context);

        RadioButtonConfig radioButtonConfig = context.getWidgetConfig();
        if (radioButtonConfig.getLayoutConfig() != null && "horizontal".equalsIgnoreCase(radioButtonConfig.getLayoutConfig().getName())) {
            result.setLayout(RadioButtonState.Layout.HORIZONTAL);
        } else {
            result.setLayout(RadioButtonState.Layout.VERTICAL);
        }

        return result;
    }
}
