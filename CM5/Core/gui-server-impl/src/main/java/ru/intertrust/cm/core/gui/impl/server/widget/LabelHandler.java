package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
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
import java.util.Date;

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
        FieldPath fieldPath = context.getFieldPaths()[0];
        if (fieldPath != null) {
            Object plainValue = context.getFieldPlainValue();
            if (plainValue == null) {
                return new LabelState("");
            }
            if (plainValue instanceof Date) {
                return new LabelState(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format((Date) plainValue));
            }
            return new LabelState(plainValue.toString());
        } else {
            LabelConfig labelConfig = context.getWidgetConfig();
            LabelState state = new LabelState(labelConfig.getText());
            state.setRelatedToRequiredField(findRelatedField(context, labelConfig));

            return state;
        }
    }

    private boolean findRelatedField(WidgetContext context, LabelConfig labelConfig) {
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

    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
