package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Iterator;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 21:15
 */
@ComponentName("label")
public class LabelWidget extends BaseWidget {

    @Override
    public Component createNew() {
        return new LabelWidget();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        LabelState labelState = (LabelState)currentState;
        Panel panel = (Panel)impl;
        Iterator<Widget> iterator = panel.iterator();
        Label redAsteriskLabel = (Label)iterator.next();
        Label label = (Label)iterator.next();
        setTrimmedText(label, labelState.getLabel());

        boolean showRedAsterisk = labelState.isRelatedToRequiredField() && isEditable;
        redAsteriskLabel.setText(showRedAsterisk ? "*" : "");
    }

    @Override
    public WidgetState getCurrentState() {
        LabelState data = new LabelState();
        Panel panel = (Panel)impl;
        Label redAsteriskLabel = (Label)panel.iterator().next();
        Label label = (Label)panel.iterator().next();
        data.setLabel(getTrimmedText(label));

        data.setRelatedToRequiredField(!redAsteriskLabel.getText().isEmpty());

        return data;
    }

    @Override
    protected Widget asEditableWidget() {
        Panel panel = new HorizontalPanel();
        Label label = new Label();
        label.setStyleName("gwt-Good-Label");
        Label redAsteriskLabel = new Label();
        redAsteriskLabel.setStyleName("gwt-Red-Label");
        panel.add(redAsteriskLabel);
        panel.add(label);

        return panel;
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

}
