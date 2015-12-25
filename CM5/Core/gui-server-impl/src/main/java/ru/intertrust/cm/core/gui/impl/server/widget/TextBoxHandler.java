package ru.intertrust.cm.core.gui.impl.server.widget;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TextBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:59
 */
@ComponentName("text-box")
public class TextBoxHandler extends ValueEditingWidgetHandler {
    @Override
    public TextState getInitialState(WidgetContext context) {
        final FieldConfig fieldConfig = getFieldConfig(context);
        boolean encrypted = fieldConfig instanceof StringFieldConfig && ((StringFieldConfig) fieldConfig).isEncrypted();
        TextState state = new TextState(context.<String>getFieldPlainValue(), encrypted);
        if (context.getWidgetConfig() instanceof TextBoxConfig) {
            setPaswordIds(context, state);
        }
        return state;
    }

    private void setPaswordIds(WidgetContext context, TextState state) {
            TextBoxConfig currentTextBoxConfig = context.getWidgetConfig();

            if (currentTextBoxConfig.getConfirmationFor() != null) {
                state.setConfirmationWidgetId(currentTextBoxConfig.getConfirmationFor().getWidgetId());
                state.setPrimaryWidgetId(currentTextBoxConfig.getId());
            }
            if (currentTextBoxConfig.getConfirmation() != null) {
                state.setPrimaryWidgetId(currentTextBoxConfig.getConfirmation().getWidgetId());
                state.setConfirmationWidgetId(currentTextBoxConfig.getId());
            }
    }


    @Override
    public Value getValue(WidgetState state) {
        return new StringValue(((TextState) state).getText());
    }
}
