package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import java.util.List;


public class CheckBoxHeaderWidget extends HeaderWidget {

    /**
     * Флаг, определяющий выделен ли чекбокс
     */
    private boolean isChecked = false;

    public CheckBoxHeaderWidget() {
        init();
    }

    public CheckBoxHeaderWidget(boolean isChecked) {
        this.isChecked = isChecked;
        init(isChecked);
    }

    @Override
    public void init() {
        setupJS(this);
        init(isChecked);
    }

    public void init(boolean isChecked) {
        setupJS(this);
        html = getTitleHtml(isChecked);
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getFilterValuesRepresentation() {
        return null;
    }

    @Override
    public List<String> getFilterValues() {
        return null;
    }

    @Override
    public void setFilterInputWidth(int filterWidth) {
    }

    @Override
    public void setFilterValuesRepresentation(String filterValue) {
    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public boolean isShowFilter() {
        return false;
    }

    @Override
    public void setShowFilter(boolean showFilter) {
    }

    @Override
    protected String getTitleHtml() {
        return getTitleHtml(false);
    }

    protected String getTitleHtml(boolean isChecked) {
        StringBuilder titleBuilder = new StringBuilder("<div  class=\"header-label\">");
        titleBuilder.append("<p style=\"overflow: hidden; text-overflow: ellipsis; " +
                "white-space: nowrap; position: relative; left: -4px\">");
        titleBuilder.append("<input type=\"checkbox\" id=\"chkall\" onclick=\"onChangeAction(this);\" ");
        if (isChecked) {
            titleBuilder.append("checked");
        }
        titleBuilder.append(" />");
        titleBuilder.append("</p></div>");
        return titleBuilder.toString();
    }

    /**
     * Обрабатывает изменение состояния чекбокса.<br>
     *
     * @param isChecked состояние чекбокса.<br>
     *                  true - поставлен, false - снят.
     */
    public void processOnChange(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    /**
     * Установка методов JavaScript
     *
     * @param inst JS-объект чекбоса.
     */
    public native void setupJS(CheckBoxHeaderWidget inst) /*-{
        $wnd.onChangeAction = function (headerCheckbox) {
            var isChecked = headerCheckbox.checked;
            $entry(inst.@ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.CheckBoxHeaderWidget::processOnChange(Ljava/lang/Boolean;)(@java.lang.Boolean::valueOf(Z)(isChecked)));
        }
    }-*/;

    /**
     * Возвращает флаг установлен ли чекбокс.
     *
     * @return true - установлен, false - снят.
     */
    public boolean isChecked() {
        return isChecked;
    }

}
