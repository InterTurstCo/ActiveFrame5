package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.dom.client.Style;
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
        applyFontStyle(label, labelState);

        boolean showRedAsterisk = labelState.isRelatedToRequiredField() && isEditable;
        redAsteriskLabel.setText(showRedAsterisk ? "*" : "");
        applyFontStyle(redAsteriskLabel, labelState);
    }

    @Override
    public WidgetState getCurrentState() {
        final LabelState initialState = getInitialData();
        LabelState state = new LabelState();
        Panel panel = (Panel)impl;
        final Iterator<Widget> iterator = panel.iterator();
        Label redAsteriskLabel = (Label) iterator.next();
        Label label = (Label) iterator.next();
        state.setLabel(getTrimmedText(label));
        state.setRelatedToRequiredField(!redAsteriskLabel.getText().isEmpty());
        state.setPattern(initialState.getPattern());
        state.setFontWeight(initialState.getFontWeight());
        state.setFontStyle(initialState.getFontStyle());
        state.setFontSize(initialState.getFontSize());

        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
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
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    private void applyFontStyle(Label label, LabelState state) {
        final String fontWeight = state.getFontWeight();
        final String fontStyle = state.getFontStyle();
        final String fontSize = state.getFontSize();
        /*if (fontWeight == null && fontStyle == null && fontSize == null) {
            return;
        }
        label.setStyleName("");*/
        final Style style = label.getElement().getStyle();
        if (fontWeight != null) {
            style.setProperty("fontWeight", fontWeight);
        }
        if (fontStyle != null) {
            style.setProperty("fontStyle", fontStyle);
        }
        if (fontSize != null) {
            style.setProperty("fontSize", fontSize);
        }
    }
}
