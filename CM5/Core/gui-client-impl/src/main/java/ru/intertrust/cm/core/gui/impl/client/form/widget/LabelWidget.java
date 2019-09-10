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

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;

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
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        LabelState labelState = (LabelState) currentState;
        Panel panel = (Panel) impl;
        Iterator<Widget> iterator = panel.iterator();
        Label redAsteriskLabel = (Label) iterator.next();
        Label label = (Label) iterator.next();
        setTrimmedText(label, labelState.getLabel());
        applyStyles(label, labelState);

        boolean showRedAsterisk = labelState.isAsteriskRequired() && isEditable;
        redAsteriskLabel.setText(showRedAsterisk ? "*" : "");
        applyFontStyle(redAsteriskLabel, labelState);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected WidgetState createNewState() {
        final LabelState initialState = getInitialData();
        LabelState state = new LabelState();
        Panel panel = (Panel) impl;
        final Iterator<Widget> iterator = panel.iterator();
        Label redAsteriskLabel = (Label) iterator.next();
        Label label = (Label) iterator.next();
        state.setLabel(getTrimmedText(label));
        state.setAsteriskRequired(!redAsteriskLabel.getText().isEmpty());
        state.setPattern(initialState.getPattern());
        state.setFontWeight(initialState.getFontWeight());
        state.setFontStyle(initialState.getFontStyle());
        state.setFontSize(initialState.getFontSize());
        state.setTextDecoration(initialState.getTextDecoration());
        state.setBackgroundColor(initialState.getBackgroundColor());
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

    private void applyStyles(Label label, LabelState state) {
        applyFontStyle(label, state);
        applyAdditionalStyles(label, state);
    }

    private void applyFontStyle(Label label, LabelState state) {
        final String fontWeight = state.getFontWeight();
        final String fontStyle = state.getFontStyle();
        final String fontSize = state.getFontSize();
        final String textDecoration = state.getTextDecoration();
        final String backgroundColor = state.getBackgroundColor();
                
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
        if (textDecoration != null) {
            style.setProperty("textDecoration", textDecoration);
        }
        if (backgroundColor != null) {
            style.setProperty("backgroundColor", backgroundColor);
        }
    }

    private void applyAdditionalStyles(Label label, LabelState state) {
        if (getDisplayConfig().getHeight() != null) {
            label.setTitle(state.getLabel() == null ? EMPTY_VALUE : state.getLabel());
            label.addStyleName("labelWidgetCut");
            label.setHeight(getDisplayConfig().getHeight());
        } else {
            label.addStyleName("labelWidgetDefault");
        }
        if (getDisplayConfig().getWidth() != null) {
            label.setWidth(getDisplayConfig().getWidth());
        }
    }

}
