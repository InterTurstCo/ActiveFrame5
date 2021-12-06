package ru.intertrust.cm.core.gui.impl.client.form.widget.colorpicker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.util.ColorUtils;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ColorPickerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Objects;

/**
 * Виджет выбора цвета из палитры<br>
 * Возвращает для последующего использования цвет в виде 6-символьного HEX-кода (без символа '#')<br>
 * <p>
 * Created by Myskin Sergey on 11.01.2021.
 */
@ComponentName("color-picker")
public class ColorPickerWidget extends BaseWidget {

    /**
     * Текстовое поле с кодом цвета (вручную не редактируется)
     */
    private TextBox colorBox;
    /**
     * Кнопка выбора цвета из палитры
     */
    private Button colorPickerButton;
    /**
     * Кнопка очистки выбранного цвета
     */
    private Button clearButton;

    @Override
    public void setValue(Object value) {
    }

    @Override
    public void disable(Boolean isDisabled) {
    }

    @Override
    public void reset() {
        colorBox.setText(null);
        setAllColors("transparent");
    }

    @Override
    public void applyFilter(String value) {
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        final String currentValue = ((ColorPickerState) currentState).getHexCode();
        colorBox.setText(currentValue);
        setAllColors(currentValue);
    }

    @Override
    public Object getValue() {
        return colorBox.getText();
    }

    @Override
    protected boolean isChanged() {
        String initValue = ((ColorPickerState) getInitialData()).getHexCode();
        final String currentValue = colorBox.getText();
        return !Objects.equals(initValue, currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        final ColorPickerState state = new ColorPickerState();
        state.setHexCode(colorBox.getText());
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return createRootWidgetPanel();
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        final HorizontalPanel rootWidgetPanel = createRootWidgetPanel();

        // в режиме чтения кнопки не нужны: делаем их неактивными и скрываем
        colorPickerButton.setEnabled(false);
        colorPickerButton.setVisible(false);

        clearButton.setEnabled(false);
        clearButton.setVisible(false);

        return rootWidgetPanel;
    }

    @Override
    public Component createNew() {
        return new ColorPickerWidget();
    }

    /**
     * Создает и возвращает основную панель виджета со всеми элементами (это, собственно, и есть сам виджет)
     *
     * @return корневую панель виджета
     */
    private HorizontalPanel createRootWidgetPanel() {
        final Label hexSymbolLabel = createHexSymbolLabel();

        colorBox = new TextBox();
        colorBox.setEnabled(false);

        initColorPickerButton();
        initClearButton();

        final HorizontalPanel hp = new HorizontalPanel();

        hp.add(hexSymbolLabel);
        hp.add(colorBox);
        hp.add(colorPickerButton);
        hp.add(clearButton);

        return hp;
    }

    /**
     * Инициализирует кнопку выбора цвета из палитры:<br>
     * создается сам объект кнопки, устанавливается текст, стиль, обработчик нажатия
     */
    private void initColorPickerButton() {
        colorPickerButton = new Button("Выбрать");
        colorPickerButton.getElement().setClassName("darkButton");

        colorPickerButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final int[] position = calculateDialogPosition();
                final int left = position[0];
                final int top = position[1];

                final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(left, top);
                final ConfirmCallback confirmCallback = new ConfirmCallback() {
                    @Override
                    public void onAffirmative() {
                        final String chosenColor = colorPickerDialog.getChosenColor();
                        // при выборе цвета устанавливаем его код в текстовое поле, сам цвет делаем фоновым, а цвет текста противоположным для лучшей читаемости
                        colorBox.setText(chosenColor);
                        setAllColors(chosenColor);
                    }

                    @Override
                    public void onCancel() {
                        // если нажали "Отмена", то ничего не делаем
                    }
                };
                colorPickerDialog.setConfirmCallback(confirmCallback);

                // устанавливаем текущий цвет в диалог, если он выбран
                final String hexColorCode = colorBox.getText();
                if ((hexColorCode != null) && !hexColorCode.isEmpty()) {
                    try {
                        colorPickerDialog.setColor(hexColorCode);
                    } catch (Exception ex) {
                        colorPickerDialog.hide();
                        ApplicationWindow.errorAlert("Задан неверный формат цвета, используйте 6ти символьный HEX-код, состоящий из символов 0-9 и A-F");
                        return;
                    }
                }
                colorPickerDialog.show();
            }
        });
    }

    /**
     * Инициализирует кнопку очистки цвета выбранного цвета:<br>
     * создается сам объект кнопки, устанавливается текст, стиль, обработчик нажатия
     */
    private void initClearButton() {
        clearButton = new Button("Сбросить");
        clearButton.getElement().setClassName("lightButton");

        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reset();
            }
        });
    }

    /**
     * Создает метку с символом '#', чтобы исключить ее из текстового кода цвета
     *
     * @return метка c символом '#'
     */
    private Label createHexSymbolLabel() {
        final Label hexSymbolLabel = new Label("#");
        hexSymbolLabel.addStyleName("hex-button-label");
        return hexSymbolLabel;
    }

    /**
     * Устанавливает все цвета: фона и текста
     *
     * @param hexColorCode 6-ти символьный hex-код цвета
     */
    private void setAllColors(String hexColorCode) {
        setBackgroundColor(hexColorCode);
        setFontColor(hexColorCode);
    }

    /**
     * Устанавливает цвет фона текстового поля с кодом цвета<br>
     *
     * @param hexColorCode 6-символьный код цвета
     */
    private void setBackgroundColor(String hexColorCode) {
        if (hexColorCode != null) {
            // когда значения нет, то цвет устанавливается прозрачный, делаем фон аналогичным
            if (hexColorCode.equalsIgnoreCase("transparent")) {
                colorBox.getElement().getStyle().setBackgroundColor("transparent");
            } else {
                if (!hexColorCode.isEmpty()) {
                    colorBox.getElement().getStyle().setBackgroundColor("#" + hexColorCode);
                } else {
                    colorBox.getElement().getStyle().setBackgroundColor("transparent");
                }
            }
        } else {
            // когда значение == null, делаем фон прозрачным
            colorBox.getElement().getStyle().setBackgroundColor("transparent");
        }
    }

    /**
     * Устанавливает цвет шрифта HEX-кода цвета:<br>
     * делает его противоположным в градациях черный-белый для лучшей читаемости
     *
     * @param hexColorCode 6-символьный HEX-код цвета
     */
    private void setFontColor(String hexColorCode) {
        if (hexColorCode != null) {
            // когда цвет не выбран (фон прозрачный), ставим обычный черный текст
            if (hexColorCode.equalsIgnoreCase("transparent")) {
                colorBox.getElement().getStyle().setColor("black");
            } else {
                if (!hexColorCode.isEmpty()) {
                    final String invertedColor = ColorUtils.invertColor(hexColorCode, true);
                    colorBox.getElement().getStyle().setColor("#" + invertedColor);
                } else {
                    // когда код цвета пуст, то просто черный текст
                    colorBox.getElement().getStyle().setColor("black");
                }
            }
        }
    }

    /**
     * Вычислить положение левого верхнего угла диалога при его отображении по следующим правилам:
     * <ul>
     *     <li>по умолчанию координаты совпадают с координатами кнопки 'Выбрать'</li>
     *     <li>если диалог не влазит по ширине (правый край выходит за видимую границу), то диалог смещается влево</li>
     *     <li>если диалог не влазит по высоте снизу (нижний край выходит за видимую границу), то диалог смещается вверх</li>
     * </ul>
     *
     * @return массив с координатами диалога: [0] - координата x; [1] - координата y (считаются от левого верхнего угла экрана);
     */
    private int[] calculateDialogPosition() {
        final int buttonLeft = colorPickerButton.getAbsoluteLeft();
        final int buttonTop = colorPickerButton.getAbsoluteTop();

        final int windowWidth = Window.getClientWidth();
        final int windowHeight = Window.getClientHeight();

        final int widthToRight = (windowWidth - buttonLeft);
        final int heightToBottom = (windowHeight - buttonTop);

        int left = buttonLeft;
        if (widthToRight < ColorPickerDialog.WIDTH) {
            left = left - ColorPickerDialog.WIDTH + 130;
        }
        int top = buttonTop;
        if (heightToBottom < ColorPickerDialog.HEIGHT) {
            top = top - ColorPickerDialog.HEIGHT + 30;
        }
        int[] position = new int[2];
        position[0] = left;
        position[1] = top;

        return position;
    }

}
