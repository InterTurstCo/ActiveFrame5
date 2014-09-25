package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 0:07
 */

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

import java.util.Date;

public class TimeBox extends Composite {

    private static final String STYLE_TIMEPICKER_ENTRY = "timePickerEntrySmall";
    private static final String STYLE_TIMEBOX_SEPARATOR = "timeboxSeparator";
    private static final String STYLE_TIMEBOX_BUTTON = "timeboxButtonUpDown";

    private ValueTextBox hoursBox;
    private ValueTextBox minutesBox;
    private ValueTextBox secondsBox;
    private ValueTextBox currentFocused;

    private boolean useSeconds;
    private FocusPanel buttonsPanelContainer;

    public TimeBox(Date time, boolean useSeconds) {

        this.useSeconds = useSeconds;
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("timeboxContainer");
        int hours = time.getHours();
        int minutes = time.getMinutes();
        int seconds = time.getSeconds();
        hoursBox = initTimeUnitBox(hours, 0, 23);
        minutesBox = initTimeUnitBox(minutes, 0, 59);
        HorizontalPanel timePanel = new HorizontalPanel();
        timePanel.setStyleName(STYLE_TIMEPICKER_ENTRY);

        timePanel.add(hoursBox);
        addSeparator(timePanel);
        timePanel.add(minutesBox);
        if (useSeconds) {
            secondsBox = initTimeUnitBox(seconds, 0, 59);
            addSeparator(timePanel);
            timePanel.add(secondsBox);
        }

        Panel buttonsPanel = new VerticalPanel();

        Label valueUp = new Label(BusinessUniverseConstants.ASCEND_ARROW);
        valueUp.setStyleName(STYLE_TIMEBOX_BUTTON);
        valueUp.addClickHandler(new TimeChanger(true));
        Label valueDown = new Label(BusinessUniverseConstants.DESCEND_ARROW);
        valueDown.setStyleName(STYLE_TIMEBOX_BUTTON);
        valueDown.addClickHandler(new TimeChanger(false));
        buttonsPanel.add(valueUp);
        buttonsPanel.add(valueDown);

        buttonsPanelContainer = new FocusPanel();
        buttonsPanelContainer.add(buttonsPanel);
        timePanel.add(buttonsPanelContainer);
        buttonsPanelContainer.setVisible(false);

        buttonsPanelContainer.addBlurHandler(new TimeUnitBlur());

        container.add(timePanel);
        initWidget(container);

    }

    private Label addSeparator(HorizontalPanel container) {
        Label separator = new Label(":");
        separator.setStyleName(STYLE_TIMEBOX_SEPARATOR);
        container.add(separator);
        container.setCellVerticalAlignment(separator, HasVerticalAlignment.ALIGN_MIDDLE);
        return separator;
    }

    private ValueTextBox initTimeUnitBox(int value, int min, int max) {
        ValueTextBox unitBox = new ValueTextBox(value, min, max);
        unitBox.setMinDigits(2);
        unitBox.setSteps(1);
        unitBox.setStyleName(STYLE_TIMEPICKER_ENTRY);
        unitBox.addDomHandler(new TimeUnitFocus(unitBox), FocusEvent.getType());

        return unitBox;
    }

    public int getHours() {
        return Integer.parseInt(hoursBox.getValue());
    }

    public int getMinutes() {
        return Integer.parseInt(minutesBox.getValue());
    }

    public int getSeconds() {
        return useSeconds ? Integer.parseInt(secondsBox.getValue()) : 0;
    }

    private class TimeChanger implements ClickHandler {
        private boolean increase;

        private TimeChanger(boolean increase) {
            this.increase = increase;
        }

        @Override
        public void onClick(ClickEvent event) {

            if (increase) {
                currentFocused.increaseValue();
            } else {
                currentFocused.decreaseValue();
            }
            currentFocused.getElement().getStyle().setBackgroundColor("cyan");
        }
    }

    private class TimeUnitFocus implements FocusHandler {
        private ValueTextBox timeUnitTextBox;

        private TimeUnitFocus(ValueTextBox timeUnitTextBox) {
            this.timeUnitTextBox = timeUnitTextBox;
        }

        @Override
        public void onFocus(FocusEvent event) {
            if (currentFocused != null) {
                currentFocused.getElement().getStyle().setBackgroundColor("white");
            }
            currentFocused = timeUnitTextBox;
            buttonsPanelContainer.setVisible(true);

        }
    }

    private class TimeUnitBlur implements BlurHandler {

        @Override
        public void onBlur(BlurEvent event) {
            buttonsPanelContainer.setVisible(false);
            currentFocused.getElement().getStyle().setBackgroundColor("white");

        }
    }

}
