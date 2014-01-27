package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.text.SimpleDateFormat;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.01.14
 *         Time: 11:40
 */
@ComponentName("country.summary.message.renderer")
public class CountryLabelRenderer extends LabelRenderer {

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {
        StringBuilder sb = new StringBuilder("custom render ");
        final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
        String displayValue = "";
        for (FieldPath fieldPath : fieldPaths) {
            Value value = context.getValue(fieldPath);
            if (value != null) {
                Object primitiveValue = value.get();
                if (primitiveValue == null) {
                    if (value instanceof LongValue || value instanceof DecimalValue) {
                        displayValue = "0";
                        sb.append(displayValue);
                        sb.append(" ");
                    }
                } else {

                    if (value instanceof DateTimeValue) {
                        displayValue = DATE_FORMATTER.format(primitiveValue);
                        sb.append(displayValue);
                        sb.append(" ");
                    } else {
                        displayValue = primitiveValue.toString();
                        sb.append(displayValue);
                        sb.append(" ");
                    }
                }
            }

        }
        return sb.toString();
    }
}

