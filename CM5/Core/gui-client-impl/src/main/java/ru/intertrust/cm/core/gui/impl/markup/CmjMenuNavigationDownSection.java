package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.markup.uploader.SimpleUploader;

public class CmjMenuNavigationDownSection extends VerticalPanel {

    public CmjMenuNavigationDownSection() {
// data for suggest box
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

        oracle.add("Подписан проект ВнД. Док #392681411.  от Шумилов А.И.!");
        oracle.add("Подписан проект ВнД. Док #352681411.  от Шумилов А.И.");
        oracle.add("Отчет об исполнении Создание отчета об исполнении Исполнение по документу # 922866951. от Журавская С.П.");
        oracle.add("Отчет об исполнении Создание отчета об исполнении Исполнение по документу # 922866913. от Журавская С.П.");
        oracle.add("Подписан проект ВнД. Док #302681451.  от Шумилов А.И.");
        oracle.add("Отчет об исполнении Создание отчета об исполнении Исполнение по документу # 922866952. от Журавская С.П.");
        oracle.add("Отчет об исполнении Создание отчета об исполнении Исполнение по документу # 922866923. от Журавская С.П.");
        oracle.add("Отчет об исполнении Создание отчета об исполнении Исполнение по документу # 922866253. от Журавская С.П.");
        oracle.add("Подписан проект ВнД. Док #302682411.  от Шумилов А.И.");
        oracle.add("Согласование прекращено в связи с подписанием документа ИсхД. Док #2869608676.  от Шумилов А.И.");
        oracle.add("Согласование прекращено в связи с подписанием документа ИсхД. Док #7869608676.  от Шумилов А.И.");
        oracle.add("Напоминание об истечении срока ознакомления ОГ. Док #3387756306. от БД 'Ознакомление'");
        oracle.add("Подписан проект ВнД. Док #302681413.  от Шумилов А.И.");
        oracle.add("Напоминание об истечении срока ознакомления ОРД. Док #711329193. от БД 'Ознакомление'");
        oracle.add("Напоминание об истечении срока ознакомления ОГ. Док #1387756606. от БД 'Ознакомление'");
        oracle.add("Напоминание об истечении срока ознакомления ОРД. Док #711329513. от БД 'Ознакомление'");

        // suggest
     //   VerticalPanel verticalPanel = new VerticalPanel();
        final SuggestBox suggestBox = new SuggestBox(oracle);

        // result
        final HorizontalPanel select = new HorizontalPanel();
        final Label result = new Label();
        final Button closeButton = new Button("X");

//        verticalPanel.add(suggestBox);
//        verticalPanel.add(select);
        this.add(suggestBox);
        this.add(select);
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                String value = event.getSelectedItem().getDisplayString();
                result.setText((value));
                select.add(result);
                select.add(closeButton);
                suggestBox.setText("");
            }

        });

        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                closeButton.removeFromParent();
                result.removeFromParent();
            }
        });
        SimpleUploader simpleUploader = new SimpleUploader();
        simpleUploader.init();
        simpleUploader.getElement().getStyle().setPaddingTop(40, Style.Unit.PX);
        simpleUploader.getElement().getStyle().setBackgroundColor("yellow");
          simpleUploader.setWidth("700px");
        this.add(simpleUploader);
       // simpleUploader.setWidth("100%");
}
}
