package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEvent;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CLOSE_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.FIND_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CLOSE_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EXTENDED_SEARCH_ERROR_MESSAGE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.FIND_BUTTON;

// визуализация плагина расширенного поиска
public class ExtendedSearchPluginView extends PluginView {
    private AbsolutePanel buttonsPanel;
    // визуализация окна расширенного поиска
    private AbsolutePanel container;
    // области поиска
    private AbsolutePanel searchAreass;
    // доменные объекты
    private AbsolutePanel domainObjects;
    // названия областей поиска(без дублирования)
    private HashSet<String> searchAreasKeysHashSet;
    // набор радио-кнопок выбора ДО (д.б. выбрана только одна)
    private HashSet<RadioButton> targetDomainObjectsRadioButtons;
    // кнопка для поиска
    private FocusPanel search = new FocusPanel();
    private FocusPanel closeSearch = new FocusPanel();
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
    private ExtSearchDialogBox extSearchDialogBox;

    private Map<String, String> valueToDisplayText;

    public ExtendedSearchPluginView(Plugin plugin, ExtendedSearchPluginData extendedSearchPluginData) {
        super(plugin);
        this.valueToDisplayText = extendedSearchPluginData.getValueToDisplayText();
        container = new AbsolutePanel();
        container.addStyleName("srch-container");

        // создание панелей для чек-боксов и радио-кнопок, определяющих условия поиска
        searchAreass = new AbsolutePanel();
        searchAreass.setStyleName("checkBoxPanel");
        domainObjects = new AbsolutePanel();
        domainObjects.setStyleName("radioBoxPanel");
        scrollSearchForm = new ScrollPanel();
        //scrollSearchForm.setHeight("435px");
        //scrollSearchForm.setWidth("650px");
        scrollSearchForm.setHeight("377px");
        search.add(new HTML("<span>" + LocalizeUtil.get(FIND_BUTTON_KEY, FIND_BUTTON) + "</span>"));
        closeSearch.add(new HTML("<span>" + LocalizeUtil.get(CLOSE_BUTTON_KEY, CLOSE_BUTTON) + "</span>"));

        // закрытие формы поиска
        closeSearch.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // удаляем окно поиска
                //Element searchWindow = DOM.getElementById("search-popup");
                //searchWindow.removeFromParent();
                extSearchDialogBox.hide();
            }
        });

        // собираем данные для поиска и отправляем в поисковый сервис
        search.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (extendedSearchFormPlugin == null) {
                    extSearchDialogBox.hide();
                } else if (isValid()) {
                // получаем данные из полей формы поиска для отправки на сервер
                    ExtendedSearchFormPluginView extendedSearchFormPluginView =
                            (ExtendedSearchFormPluginView) extendedSearchFormPlugin.getView();
                    Map<String, WidgetState> widgetsWithData = extendedSearchFormPluginView.getWidgetsState();
                    searchFormData.setMaxResults(200); // временно хардкодом, в форме д.б. поле для ввода размера списка результатов
                    searchFormData.setSearchQuery(searchQuery);
                    searchFormData.setFormWidgetsData(widgetsWithData);
                    sendSearchData(searchFormData);
                }
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

        while (keySetIterator.hasNext()) {
            String key = keySetIterator.next();
            ArrayList<String> areasDomainObjects = searchAreasData.get(key);
            // исключаем дублирование при отображении областей поиска
            if (!searchAreasKeysHashSet.contains(key)) {
                searchAreasKeysHashSet.add(key);
                cb = new CheckBox(getDisplayTextByValue(key));
                cb.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        CheckBox checkBox = (CheckBox) event.getSource();
                        boolean checked = checkBox.getValue();
                        String text = checkBox.getText();
                        String searchAreaName = getValueByDisplayText(text);
                        if (checked) {
                            // "чистим" все кнопки выбора объекта
                            for (RadioButton rb : targetDomainObjectsRadioButtons) {
                                rb.setValue(false);
                            }
                            // выбираем область поиска
                            searchQuery.getAreas().add(searchAreaName);
                        } else {
                            // если область поиска "убрали" - удаляем ее из условий поиска
                            searchQuery.getAreas().remove(searchAreaName);
                        }
                    }
                });
                AbsolutePanel cbDecorated = new AbsolutePanel();
                cbDecorated.setStyleName("srch-cb-decorated");
                searchAreass.add(cbDecorated);
                cbDecorated.add(cb);
            }

            for (int i = 0; i < areasDomainObjects.size(); i++) {
                // key - это группа(область поиска) к которой кнопка относится
                rb = new RadioButton(key, getDisplayTextByValue(areasDomainObjects.get(i)));
                rb.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        RadioButton radioButton = (RadioButton) event.getSource();
                        boolean selected = radioButton.getValue();
                        if (selected) {
                            for (RadioButton rb : targetDomainObjectsRadioButtons) {
                                rb.setValue(false);
                            }
                            // выбираем целевой доменный объект для поиска
                            String targetObjectType = getValueByDisplayText(radioButton.getText());
                            searchQuery.setTargetObjectType(targetObjectType);
                            // вызываем форму поиска
                            invokeSearchForm(searchQuery);
                            // устанавливаем кнопку
                            radioButton.setValue(true);
                        }
                    }
                });

                targetDomainObjectsRadioButtons.add(rb);

                AbsolutePanel rbDecorated = new AbsolutePanel();
                rbDecorated.setStyleName("srch-rb-decorated");
                rbDecorated.add(rb);
                domainObjects.add(rbDecorated);
            }
        }

        container.add(searchAreass);
        container.add(domainObjects);
        container.add(scrollSearchForm);
        buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("srch-buttons-panel");
        searchButtonStyles(search);
        applyButtonStyles(closeSearch);
        buttonsPanel.add(search);
        buttonsPanel.add(closeSearch);
        container.add(buttonsPanel);
        container.getElement().getStyle().clearOverflow();
    }

    public SearchPopup getSearchPopup() {
        return searchPopup;
    }

    public void setSearchPopup(SearchPopup searchPopup) {
        this.searchPopup = searchPopup;
    }

    public void setSearchPopup(ExtSearchDialogBox extSearchDialogBox) {
        this.extSearchDialogBox = extSearchDialogBox;
    }

    // отправка данных в поисковый сервис
    public void sendSearchData(ExtendedSearchData extendedSearchData) {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto extendedSearchData) {
                //Window.alert(" Данные расширенного поиска ОТПРАВЛЕНЫ !");
                // удаляем окно поиска
                extSearchDialogBox.hide();
                Application.getInstance().getEventBus().fireEvent(
                        new ExtendedSearchCompleteEvent((DomainObjectSurferPluginData) extendedSearchData));
            }

            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.EXTENDED_SEARCH_ERROR_MESSAGE_KEY,
                        EXTENDED_SEARCH_ERROR_MESSAGE) + caught.getMessage());
            }
        };
        Command command = new Command("searchFormDataProcessor", this.plugin.getName(), extendedSearchData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback);
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
        extendSearchFormPluginPanel.setClassForPluginPanel("ext-search-content-wrapper");

        extendSearchFormPluginPanel.asWidget().getElement().getStyle().clearOverflow();
        scrollSearchForm.clear();
        scrollSearchForm.add(extendSearchFormPluginPanel);
    }

    @Override
    public IsWidget getViewWidget() {
        return container;
    }

    private void applyButtonStyles(FocusPanel button) {
        button.addStyleName("auth_enter-button");
    }

    private void searchButtonStyles(FocusPanel button) {
        button.addStyleName("auth_enter-button-search");
    }

    private boolean isValid() {
            ExtendedSearchFormPluginView extendedSearchFormPluginView =
                (ExtendedSearchFormPluginView) extendedSearchFormPlugin.getView();
        FormPanel panel = (FormPanel)extendedSearchFormPluginView.getViewWidget();

        ValidationResult validationResult = new ValidationResult();
        for (BaseWidget widget : panel.getWidgets()) {
            if (widget.isEditable()) {
                validationResult.append(widget.validate());
            }
        }
        if (validationResult.hasErrors()) {
            ApplicationWindow.errorAlert(LocalizeUtil.get(CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY,
                    CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE));
            return false;
        }
        return true;
    }

    private String getDisplayTextByValue(String value) {
        String displayText = valueToDisplayText.get(value);
        return displayText != null ? displayText : value;
    }

    private String getValueByDisplayText(String displayText) {
        for (Map.Entry<String, String> entry : valueToDisplayText.entrySet()) {
            if (displayText.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return displayText;
    }
}
