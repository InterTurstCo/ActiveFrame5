package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class CmjMenuNavigationForSplitPanelTable extends HorizontalPanel {
    FlexTable flexTable;

    public CmjMenuNavigationForSplitPanelTable() {
        flexTable = new FlexTable();
        headerForTable();
        for (int i = 1; i < 20; i++) {
            fakeContent(i);
        }

        this.add(flexTable);
    }

    FlexTable headerForTable() {
        flexTable.setWidget(0, 0, new TableWidgetLoupe("Создано"));
        flexTable.setWidget(0, 1, new Image(""));
        flexTable.setWidget(0, 2, new Image(""));
        flexTable.setWidget(0, 3, new TableWidgetLoupe("Срок"));
        flexTable.setWidget(0, 4, new TableWidgetLoupe("Тип задачи"));
        flexTable.setWidget(0, 5, new TableWidgetLoupe("От кого"));
        flexTable.setWidget(0, 6, new TableWidgetLoupe("Кому"));
        flexTable.setWidget(0, 7, new TableWidgetLoupe("Номер"));
        flexTable.setWidget(0, 8, new TableWidgetLoupe("Подписант"));
        flexTable.setWidget(0, 9, new TableWidgetLoupe("Заголовок"));
        flexTable.setWidget(0, 10, new TableWidgetLoupe("Текущий статус"));
        flexTable.setWidget(0, 11, new TableWidgetLoupe("Запланировано"));
        flexTable.setWidget(0, 12, new TableWidgetLoupe("Исполнитель"));
        return flexTable;
    }

    FlexTable fakeContent(int row) {

        flexTable.setWidget(row, 0, new Label("Сегодня"));
        flexTable.setWidget(row, 1, new Label(""));
        flexTable.setWidget(row, 2, new Label(""));
        flexTable.setWidget(row, 3, new Label("Завтра"));
        flexTable.setWidget(row, 4, new Label("На согласование"));
        flexTable.setWidget(row, 5, new Label("Масан Б. А."));
        flexTable.setWidget(row, 6, new Label("Масан Б. А."));
        flexTable.setWidget(row, 7, new Label(""));
        flexTable.setWidget(row, 8, new Label(""));
        flexTable.setWidget(row, 9, new Label("Резолюция №1212121212"));
        flexTable.setWidget(row, 10, new Label(""));
        flexTable.setWidget(row, 11, new Label(""));
        flexTable.setWidget(row, 12, new Label(""));
        return flexTable;
    }
}
