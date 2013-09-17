package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.TableWidgetLoupe;

public class CollectionViewerPluginView extends PluginView {

    protected CollectionViewerPluginView(CollectionViewerPlugin plugin) {
        super(plugin);
    }

    @Override
    protected IsWidget getViewWidget() {
        VerticalPanel container = new VerticalPanel();
        container.getElement().getStyle().setProperty("backgroundColor", "#EEE");

        Label labelTask = new Label("Задачи");
        Label labelInput = new Label("Поступившие");
        Label labelUnproc = new Label("Необработанные");
        container.add(labelTask);
        container.add(labelInput);
        container.add(labelUnproc);

        FlexTable flexTable = new FlexTable();
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

        for (int i = 1; i < 5; i++) {

            flexTable.setWidget(i, 0, new Label("Сегодня"));
            flexTable.setWidget(i, 1, new Label(""));
            flexTable.setWidget(i, 2, new Label(""));
            flexTable.setWidget(i, 3, new Label("Завтра"));
            flexTable.setWidget(i, 4, new Label("На согласование"));
            flexTable.setWidget(i, 5, new Label("Масан Б. А."));
            flexTable.setWidget(i, 6, new Label("Масан Б. А."));
            flexTable.setWidget(i, 7, new Label(""));
            flexTable.setWidget(i, 8, new Label(""));
            flexTable.setWidget(i, 9, new Label("Резолюция №1212121212"));
            flexTable.setWidget(i, 10, new Label(""));
            flexTable.setWidget(i, 11, new Label(""));
            flexTable.setWidget(i, 12, new Label(""));
        }

        container.add(flexTable);

        return container;
    }
}
