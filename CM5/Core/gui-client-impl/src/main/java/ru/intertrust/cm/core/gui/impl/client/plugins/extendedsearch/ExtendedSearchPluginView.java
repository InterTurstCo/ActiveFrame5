package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// визуализация плагина расширенного поиска
public class ExtendedSearchPluginView extends PluginView {

    private VerticalPanel container;
    private ListBox searchAreas; // это приходит с сервера

    // данные о конфигурации областей поиска и доменных объектах
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    // данные о полях для поиска
    private HashMap<String, ArrayList<String>> searchFieldsData = new HashMap<String, ArrayList<String>>();

    public ExtendedSearchPluginView(Plugin plugin, ExtendedSearchPluginData extendedSearchPluginData) {
        super(plugin);
        container = new VerticalPanel();
        container.setWidth("600px");

        // что же приходит нам с сервера ?
        searchAreas = new ListBox();
        searchAreas.setWidth("400px");

        // текстовая область для просмотра пришедших данных для поиска
        TextArea textArea = new TextArea();
        textArea.setVisibleLines(20);
        textArea.setWidth("500px");

        // получаем данные про области поиска
        this.searchAreasData = extendedSearchPluginData.getSearchAreasData();
        Iterator<String> keySetIterator = searchAreasData.keySet().iterator();

        while(keySetIterator.hasNext()){
            String key = keySetIterator.next();
            ArrayList<String> value = searchAreasData.get(key);

            /*for (Iterator<String> i = value.iterator(); i.hasNext();)
                searchAreas.addItem("Обл.поиска: " + key + " - Целевой ДО: " +  i.next());
            */
            for (int i = 0; i < value.size(); i++) {
                searchAreas.addItem("Обл: " + key + "  и ДО: " +  value.get(i));
            }
        }

        // получаем данные про поля для поиска
        this.searchFieldsData = extendedSearchPluginData.getSearchFieldsData();
        Iterator<String> keySetFieldsIterator = searchFieldsData.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        while(keySetFieldsIterator.hasNext()){
            String key = keySetFieldsIterator.next();
            ArrayList<String> value = searchFieldsData.get(key);

            for (int i = 0; i < value.size(); i++) {
                sb.append("\n ДО: " + key + " его Поле: " +  value.get(i));
            }
        }

        textArea.setText(sb.toString());

        container.add(searchAreas);
        container.add(textArea);
    }

    @Override
    public IsWidget getViewWidget() {

        return container;
    }
}
