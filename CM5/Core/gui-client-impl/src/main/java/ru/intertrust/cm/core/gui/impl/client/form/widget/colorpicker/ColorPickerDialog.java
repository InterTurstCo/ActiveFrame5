package ru.intertrust.cm.core.gui.impl.client.form.widget.colorpicker;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.util.ColorUtils;

/**
 * Диалог выбора цвета из палитры виджета 'color-picker'<br>
 * <br>
 * Created by Myskin Sergey on 12.01.2021.
 */
class ColorPickerDialog extends DialogBox {

    // ширина и высота диалога
    static final int WIDTH = 520;
    static final int HEIGHT = 415;

    private ColorPicker picker;
    // координаты положения диалога относительно верхнего левого угла в пикселях
    private Integer left;
    private Integer top;

    /**
     * Параметр действий, которые будут выполнены при нажатии на кнопки диалога
     */
    private ConfirmCallback confirmCallback;

    ColorPickerDialog() {
        init();
    }

    ColorPickerDialog(int left, int top) {
        this.left = left;
        this.top = top;

        init();
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        final int eventTypeCode = event.getTypeInt();

        // обрабатываем нажатие кнопок клавиатуры
        if (eventTypeCode == Event.ONKEYDOWN) {
            final int keyCode = event.getNativeEvent().getKeyCode();

            // при нажатии Esc делаем тоже самое, что и при нажатии на кнопку отмены
            if (keyCode == KeyCodes.KEY_ESCAPE) {
                onCancel();
                // при нажатии Enter делаем тоже самое, что и при нажатии на кнопку принятия ('Ок')
            } else if (keyCode == KeyCodes.KEY_ENTER) {
                onConfirm();
            }
        }
    }

    /**
     * Инициализирует диалог:<br>
     * добавляются элементы, устанавливаются стили, положение диалога при показе
     */
    private void init() {
        initDialogPosition();

        setText("Выберите цвет");
        setTitle("Выберите цвет");

        initColorPicker();

        // Define the panels
        VerticalPanel rootPanel = new VerticalPanel();
        FlowPanel buttonsPanel = createButtonsPanel();

        // Put it together
        rootPanel.add(picker);
        rootPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rootPanel.add(buttonsPanel);

        setWidget(rootPanel);
        setPopupPosition(left, top);

        setStyleName("infoDialogWindow");
        addStyleName("colorPickerDialog");
    }

    /**
     * Инициализирует координаты положения диалога при показе:<br>
     * если они не установлены (== null), то берутся значения для показа диалога в центре экрана
     */
    private void initDialogPosition() {
        if ((left == null) || (top == null)) {
            final int windowWidth = Window.getClientWidth();
            final int windowHeight = Window.getClientHeight();

            this.left = (windowWidth / 2) - (WIDTH / 2);
            this.top = (windowHeight / 2) - (HEIGHT / 2);
        }
    }

    /**
     * Создает и возвращает панель с кнопками
     *
     * @return объект панели с кнопками
     */
    private FlowPanel createButtonsPanel() {
        FlowPanel buttonsPanel = new FlowPanel();
        buttonsPanel.setStyleName("colorPickerDialogButtonsPanel");

        // Define the buttons
        Button okButton = createOkButton();
        Button cancelButton = createCancelButton();

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        return buttonsPanel;
    }

    /**
     * Создает и возвращает кнопку 'Отменить'
     *
     * @return объект кнопки 'Отменить'
     */
    private Button createCancelButton() {
        Button cancelButton = new Button("Отмена");   // cancel button
        cancelButton.setStyleName("lightButton");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onCancel();
            }
        });
        return cancelButton;
    }

    /**
     * Выполняет действия при инициации действия 'Отменить'
     */
    private void onCancel() {
        if (confirmCallback != null) {
            confirmCallback.onCancel();
        }
        hide();
    }

    /**
     * Создает и возвращает кнопку подтверждения ('Ок')
     *
     * @return объект кнопки 'Ок'
     */
    private Button createOkButton() {
        Button okButton = new Button("Ок");   // ok button
        okButton.setStyleName("darkButton");
        okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onConfirm();
            }
        });
        return okButton;
    }

    /**
     * Выполняет действия при инициации действия подтверждения.
     */
    private void onConfirm() {
        final boolean isHexCodeValid = validateHexCodeValue();

        // прерываем дальнейшее выполнение, если формат цвета задан неверно
        if (isHexCodeValid) {
            if (confirmCallback != null) {
                confirmCallback.onAffirmative();
            }
            hide();
        }
    }

    /**
     * Инициализирует компонент выбора цвета из палитры
     */
    private void initColorPicker() {
        picker = new ColorPicker();

        final TextBox hexColorTextBox = picker.getTbHexColor();
        // при изменении цвета вручную (путем ввода с клавиатуры с текстовое поле) производим проверку правильности формата введенного HEX-кода цвета
        hexColorTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateHexCodeValue();
            }
        });
        hexColorTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validateHexCodeValue();
            }
        });

        // при выборе цвета из палитры все равно делаем валидацию на случай, если до того было введено неверное значение
        final SliderMap slidermap = picker.getSlidermap();
        slidermap.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                validateHexCodeValue();
            }
        });
    }

    /**
     * Проверяет HEX-код введенного цвета и возвращает результат:<br>
     * если формат неверный, граница поля устанавливается красной; иначе ставится стандартного цвета
     *
     * @return true - формат цвета верный; false - неверный
     */
    private boolean validateHexCodeValue() {
        final TextBox hexColorTextBox = picker.getTbHexColor();
        final String hexCode = hexColorTextBox.getText();

        if (ColorUtils.validateHexColorCode6symbolFormat(hexCode)) {
            hexColorTextBox.getElement().getStyle().setBorderColor("#cccccc");
            return true;
        } else {
            hexColorTextBox.getElement().getStyle().setBorderColor("red");
            return false;
        }
    }

    /**
     * Возвращает HEX-код выбранного цвета
     *
     * @return 6-символьный HEX-код выбранного цвета
     */
    String getChosenColor() {
        return picker.getHexColor();
    }

    /**
     * Устанавливает цвет в виджет
     *
     * @param hexColorCode 6-символьный HEX-код цвета
     * @throws Exception исключение в случае неверного формата цвета
     */
    void setColor(String hexColorCode) throws Exception {
        picker.setHex(hexColorCode);
    }

    /**
     * Устанавливает объект действия, которые будут выполнены при подтверждении и отмене
     *
     * @param confirmCallback объект действия
     */
    void setConfirmCallback(ConfirmCallback confirmCallback) {
        this.confirmCallback = confirmCallback;
    }

}
