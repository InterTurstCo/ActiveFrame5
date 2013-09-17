package ru.intertrust.cm.core.gui.impl.client.plugins.searchpanel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class SearchPluginView extends PluginView {
    protected SearchPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected IsWidget getViewWidget() {
        HorizontalPanel container = new HorizontalPanel();
        ListBox search = new ListBox();
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");
        search.addItem("Подписан проект Исходящий документ для шаблона + Локер от  Преображенская  М.М. ");

        search.setVisibleItemCount(1);
        container.add(search);

        Image magnifierImage = new Image("css/i/ext-search.png");
        magnifierImage.getElement().getStyle().setProperty("marginRight", "30px");
        container.add(magnifierImage);
        return container;
    }
}
