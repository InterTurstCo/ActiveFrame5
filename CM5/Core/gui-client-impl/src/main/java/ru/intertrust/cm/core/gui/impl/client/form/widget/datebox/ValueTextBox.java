package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 0:09
 */

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.TextBox;


public class ValueTextBox extends TextBox {

    private int min = 0;
    private int max = 100;
    private boolean minConstrained = true;
    private boolean maxConstrained = true;
    private int minDigits = 1;
    private int step = 1;

    public ValueTextBox(int value) {
        this(value, 0, 100);
    }

    public ValueTextBox(int value, int min, int max) {
        this(value, min, max, true);
    }

    public ValueTextBox(int value, int min, int max, boolean constrained) {
        this(value, min, max, constrained, constrained);
    }

    public ValueTextBox(int value, int min, int max, boolean minConstrained,
                        boolean maxConstrained) {
        super();

        addKeyPressHandler(keyPressHandler);
        addKeyUpHandler(keyUpHandler);

        this.min = min;
        this.max = max;
        this.minConstrained = minConstrained;
        this.maxConstrained = maxConstrained;

        setValue(formatValue(value), false);
        setAlignment(TextAlignment.CENTER);
    }

    public void setMinDigits(int minDigits) {
        if (minDigits > 0) {
            this.minDigits = minDigits;

            String value = getText();
            long newValue = parseValue(value);

            setText(formatValue(newValue));
        }
    }

    public void setSteps(int step) {
        this.step = step;
    }

    protected void increaseValue() {

            String value = getText();
            long newValue = parseValue(value);
            newValue += step;
            if (maxConstrained && (newValue > max)) {
               setValue(formatValue(min));
            }
            setValue(formatValue(newValue));

    }

    protected void decreaseValue() {

            String value = getText();
            long newValue = parseValue(value);
            newValue -= step;
            if (minConstrained && (newValue < min)) {
                setValue(formatValue(max));
            }
            setValue(formatValue(newValue));

    }

    /**
     * @param value
     *            the value to format
     * @return the formatted value
     */
    protected String formatValue(long value) {
        String newValue = String.valueOf(value);

        if (minDigits > newValue.length()) {
            String leading = repeat("0",
                    (minDigits - newValue.length()));
            newValue = leading + newValue;
        }

        return newValue;
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        try {
            long newValue = parseValue(value);
            if ((maxConstrained && (newValue > max))
                    || (minConstrained && (newValue < min))) {
                return;
            }
            String prevText = getValue();
            super.setText(formatValue(newValue));
            if (fireEvents) {
                ValueChangeEvent.fireIfNotEqual(this, getValue(), prevText);
            }
        } catch (Exception ex) {
            // Do Nothing
        }
    }

    /**
     * @param value
     *            the value to parse
     * @return the parsed value
     */
    protected long parseValue(String value) {
        return Long.valueOf(value);
    }
    public  String repeat(String s, int n) {
        if (s == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private KeyUpHandler keyUpHandler = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (isReadOnly() || !isEnabled()) {
                return;
            }

            int keyCode = event.getNativeEvent().getKeyCode();

            boolean processed = false;

            switch (keyCode) {
                case KeyCodes.KEY_LEFT:
                case KeyCodes.KEY_RIGHT:
                case KeyCodes.KEY_BACKSPACE:
                case KeyCodes.KEY_DELETE:
                case KeyCodes.KEY_TAB:
                    if (getText().isEmpty()) {
                        setValue(formatValue(min));
                    }
                    return;
                case KeyCodes.KEY_UP:
                    if (step != 0) {
                        increaseValue();
                        processed = true;
                    }
                    break;
                case KeyCodes.KEY_DOWN:
                    if (step != 0) {
                        decreaseValue();
                        processed = true;
                    }
                    break;
            }

            if (processed) {
                cancelKey();
            }
        }

    };

    private KeyPressHandler keyPressHandler = new KeyPressHandler() {
        @Override
        public void onKeyPress(KeyPressEvent event) {

            if (isReadOnly() || !isEnabled()) {
                return;
            }

            int keyCode = event.getNativeEvent().getKeyCode();

            switch (keyCode) {
                case KeyCodes.KEY_LEFT:
                case KeyCodes.KEY_RIGHT:
                case KeyCodes.KEY_BACKSPACE:
                case KeyCodes.KEY_DELETE:
                case KeyCodes.KEY_TAB:
                case KeyCodes.KEY_UP:
                case KeyCodes.KEY_DOWN:
                    return;
            }

            int index = getCursorPos();
            String previousText = getText();
            String newText;
            if (getSelectionLength() > 0) {
                newText = previousText.substring(0, getCursorPos())
                        + event.getCharCode()
                        + previousText.substring(getCursorPos()
                        + getSelectionLength(), previousText.length());
            } else {
                newText = previousText.substring(0, index)
                        + event.getCharCode()
                        + previousText.substring(index, previousText.length());
            }
            cancelKey();

            setValue(newText, true);
        }
    };

}

