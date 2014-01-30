package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEvent;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

// визуализация плагина расширенного поиска
public class ExtendedSearchPluginView extends PluginView {
      private AbsolutePanel buttonsPanel;
    // визуализация окна расширенного поиска
    private VerticalPanel container;
    // области поиска
    private HorizontalPanel searchAreass;
    // доменные объекты
    private HorizontalPanel domainObjects;
    // названия областей поиска(без дублирования)
    private HashSet<String> searchAreasKeysHashSet;
    // набор радио-кнопок выбора ДО (д.б. выбрана только одна)
    private HashSet<RadioButton> targetDomainObjectsRadioButtons;
    // кнопка для поиска
    private Button search = new Button("Найти");
    private Button closeSearch = new Button("Закрыть");
    private CheckBox cb;    // для выбора области поиска
    private RadioButton rb; // для выбора целевого доменного объекта
    private ExtendedSearchFormPlugin extendedSearchFormPlugin;
    private ScrollPanel scrollSearchForm; // для формы поиска
    private PluginPanel extendSearchFormPluginPanel;
    // данные о конфигурации областей поиска и доменных объектах
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    // данные о полях для поиска
    private HashMap<String, ArrayList<String>> searchFieldsData = new HashMap<String, ArrayList<String>>();
    // объект для сбора условий поиска на форме поиска
    private SearchQuery searchQuery = new SearchQuery();
    // данные условий расширенного поиска
    private ExtendedSearchData searchFormData = new ExtendedSearchData();
    private SearchPopup searchPopup;

    public ExtendedSearchPluginView(Plugin plugin, ExtendedSearchPluginData extendedSearchPluginData) {
        super(plugin);
        container = new VerticalPanel();
        container.setWidth("800px");

        // создание панелей для чек-боксов и радио-кнопок, определяющих условия поиска
        searchAreass = new HorizontalPanel();
        domainObjects = new HorizontalPanel();
        scrollSearchForm = new ScrollPanel();
        scrollSearchForm.setHeight("340px");
        scrollSearchForm.setWidth("700px");

        // закрытие формы поиска
     closeSearch.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // удаляем окно поиска
                //Element searchWindow = DOM.getElementById("search-popup");
                //searchWindow.removeFromParent();
                searchPopup.hide();
            }
        });

        // собираем данные для поиска и отправляем в поисковый сервис
        search.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // получаем данные из полей формы поиска для отправки на сервер
                ExtendedSearchFormPluginView extendedSearchFormPluginView =
                                                       (ExtendedSearchFormPluginView) extendedSearchFormPlugin.getView();
                Map<String, WidgetState> widgetsWithData = extendedSearchFormPluginView.getWidgetsState();
                searchFormData.setMaxResults(20);
                searchFormData.setSearchQuery(searchQuery);
                searchFormData.setFormWidgetsData(widgetsWithData);
                sendSearchData(searchFormData);
            }
        });

        // получаем данные про имена возвращаемых коллекций при поиске
        searchFormData.setTargetCollectionNames(extendedSearchPluginData.getTargetCollectionNames());
        // получаем данные про области поиска
        this.searchAreasData = extendedSearchPluginData.getSearchAreasData();
        Iterator<String> keySetIterator = searchAreasData.keySet().iterator();
        // названия областей поиска
        searchAreasKeysHashSet = new HashSet<String>();
        // целевой доменный объект
        targetDomainObjectsRadioButtons = new HashSet<RadioButton>();

        while(keySetIterator.hasNext()){
            String key = keySetIterator.next();
            ArrayList<String> areasDomainObjects = searchAreasData.get(key);
            // исключаем дублирование при отображении областей поиска
            if (!searchAreasKeysHashSet.contains(key)) {
                searchAreasKeysHashSet.add(key);
                cb = new CheckBox(key);
                cb.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        boolean checked = ((CheckBox) event.getSource()).getValue();
                        if (checked) {
                            // "чистим" все кнопки выбора объекта
                            for (Iterator<RadioButton> it = targetDomainObjectsRadioButtons.iterator(); it.hasNext();)
                                it.next().setValue(false);
                            // выбираем область поиска
                            searchQuery.getAreas().add(((CheckBox) event.getSource()).getText());
                        } else {
                            // если область поиска "убрали" - удаляем ее из условий поиска
                            searchQuery.getAreas().remove((((CheckBox) event.getSource()).getText()));
                        }
                    }
                });
                searchAreass.add(cb);
            }

            for (int i = 0; i < areasDomainObjects.size(); i++) {
                // key - это группа(область поиска) к которой кнопка относится
                rb = new RadioButton(key, areasDomainObjects.get(i));
                rb.addClickHandler(new ClickHandler(){
                    @Override
                    public void onClick(ClickEvent event) {
                        boolean selected = ((RadioButton) event.getSource()).getValue();
                        if (selected) {
                            for (Iterator<RadioButton> it = targetDomainObjectsRadioButtons.iterator(); it.hasNext();)
                                it.next().setValue(false);
                            // выбираем целевой доменный объект для поиска
                            searchQuery.setTargetObjectType(((RadioButton) event.getSource()).getText());
                            // вызываем форму поиска
                            invokeSearchForm(searchQuery);
                            // устанавливаем кнопку
                            ((RadioButton) event.getSource()).setValue(true);
                        }
                    }
                });

                targetDomainObjectsRadioButtons.add(rb);
                domainObjects.add(rb);
            }
        }

       container.add(searchAreass);
        container.add(domainObjects);
        container.add(scrollSearchForm);
        buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("buttons-panel");
        applyButtonStyles(search);
        applyButtonStyles(closeSearch);
        buttonsPanel.add(search);
        buttonsPanel.add(closeSearch);
        container.add(buttonsPanel);
        container.getElement().getStyle().clearOverflow();
        //  container.add(closeSearch);
    }

    public SearchPopup getSearchPopup() {
        return searchPopup;
    }

    public void setSearchPopup(SearchPopup searchPopup) {
        this.searchPopup = searchPopup;
    }

    // отправка данных в поисковый сервис
    public void sendSearchData(ExtendedSearchData extendedSearchData) {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto extendedSearchData) {
                //Window.alert(" Данные расширенного поиска ОТПРАВЛЕНЫ !");
                // удаляем окно поиска
                searchPopup.hide();
                Application.getInstance().getEventBus().fireEvent(
                                     new ExtendedSearchCompleteEvent((DomainObjectSurferPluginData)extendedSearchData));
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(" Ошибка расширенного поиска " + caught.getMessage());
            }
        };
        Command command = new Command("searchFormDataProcessor", this.plugin.getName(), extendedSearchData);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }

    // Вызов формы поиска, которая соответствует целевому доменному объекту и всем условиям поиска
    public void invokeSearchForm(SearchQuery searchQuery) {
        // удаляем прошлую форму поиска
      //  if (extendSearchFormPluginPanel != null)
           // container.remove(extendSearchFormPluginPanel);
     /*   if (scrollSearchForm != null)
            container.remove(scrollSearchForm) ;*/

        ExtendedSearchData extendedSearchData = new ExtendedSearchData();
        extendedSearchData.setSearchQuery(searchQuery);
        extendSearchFormPluginPanel = new PluginPanel();

        extendedSearchFormPlugin = ComponentRegistry.instance.get("extended.search.form.plugin");
        extendedSearchFormPlugin.setConfig(extendedSearchData);
        extendSearchFormPluginPanel.open(extendedSearchFormPlugin);

        extendSearchFormPluginPanel.asWidget().getElement().getStyle().clearOverflow();
        scrollSearchForm.clear();
        scrollSearchForm.add(extendSearchFormPluginPanel);
}

    @Override
    public IsWidget getViewWidget() {
        return container;
    }
    private void applyButtonStyles(Button button) {
        button.addStyleName("dialog-box-button");
        button.removeStyleName("gwt-Button");
    }
}
