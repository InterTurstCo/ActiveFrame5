package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CLOSE_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.FIND_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CLOSE_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EXTENDED_SEARCH_ERROR_MESSAGE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.FIND_BUTTON;

import java.util.*;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.search.ExtendedSearchPopupConfig;
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
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchDomainObjectSurfacePluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

// визуализация плагина расширенного поиска
public class ExtendedSearchPluginView extends PluginView {
    private final AbsolutePanel buttonsPanel;
    // визуализация окна расширенного поиска
    private final AbsolutePanel container;
    // области поиска
    private final AbsolutePanel searchAreass;
    // доменные объекты
    private final AbsolutePanel domainObjects;

    // набор радио-кнопок выбора ДО (д.б. выбрана только одна)
    private final LinkedHashMap<String, RadioButton> targetDomainObjectsRadioButtons;
    // кнопка для поиска
    private final FocusPanel search = new FocusPanel();
    private final FocusPanel closeSearch = new FocusPanel();
    private Map<String,  CheckBox> searchAreaCheckBoxes = new HashMap<>();
    private RadioButton rb; // для выбора целевого доменного объекта
    private ExtendedSearchFormPlugin extendedSearchFormPlugin;
    private final AbsolutePanel scrollSearchForm; // для формы поиска
    private final ScrollPanel searchPanel; // для формы поиска
    private PluginPanel extendSearchFormPluginPanel;
    // данные о конфигурации областей поиска и доменных объектах
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    // объект для сбора условий поиска на форме поиска
    private final SearchQuery searchQuery = new SearchQuery();
    // данные условий расширенного поиска
    private final ExtendedSearchData searchFormData = new ExtendedSearchData();
    private SearchPopup searchPopup;
    private ExtSearchDialogBox extSearchDialogBox;

    private final Map<String, String> valueToDisplayText;
    private final ExtendedSearchPopupConfig popupConfig;

    public ExtendedSearchPluginView(Plugin plugin, ExtendedSearchPluginData extendedSearchPluginData) {
        super(plugin);
        this.valueToDisplayText = extendedSearchPluginData.getValueToDisplayText();
        this.popupConfig = extendedSearchPluginData.getExtendedSearchPopupConfig();
        container = new AbsolutePanel();
        container.addStyleName("srch-container");

        searchPanel = new ScrollPanel();

        // создание панелей для чек-боксов и радио-кнопок, определяющих условия
        // поиска
        searchAreass = new AbsolutePanel();
        searchAreass.setStyleName("checkBoxPanel");
        domainObjects = new AbsolutePanel();
        domainObjects.setStyleName("radioBoxPanel");
        scrollSearchForm = new AbsolutePanel();

        setupFormSizes(ExtendedSearchDialogHelper.getHeight(popupConfig), ExtendedSearchDialogHelper.getWidth(popupConfig));

        search.add(new HTML("<span>" + LocalizeUtil.get(FIND_BUTTON_KEY, FIND_BUTTON) + "</span>"));
        closeSearch.add(new HTML("<span>" + LocalizeUtil.get(CLOSE_BUTTON_KEY, CLOSE_BUTTON) + "</span>"));

        // закрытие формы поиска
        closeSearch.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // удаляем окно поиска
                // Element searchWindow = DOM.getElementById("search-popup");
                // searchWindow.removeFromParent();
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
                    // получаем данные из полей формы поиска для отправки на
                    // сервер
                    ExtendedSearchFormPluginView extendedSearchFormPluginView =
                            (ExtendedSearchFormPluginView) extendedSearchFormPlugin.getView();
                    Map<String, WidgetState> widgetsWithData = extendedSearchFormPluginView.getWidgetsState();
                    searchFormData.setMaxResults(200); // временно хардкодом, в
                                                       // форме д.б. поле для
                                                       // ввода размера списка
                                                       // результатов
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
        searchAreaCheckBoxes = new HashMap<>();
        // целевой доменный объект
        targetDomainObjectsRadioButtons = new LinkedHashMap<>();

        while (keySetIterator.hasNext()) {
            String key = keySetIterator.next();
            ArrayList<String> areasDomainObjects = searchAreasData.get(key);
            // исключаем дублирование при отображении областей поиска
            if (!searchAreaCheckBoxes.containsKey(key)) {
                CheckBox cb = new CheckBox(getDisplayTextByValue(key));
                cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                        CheckBox checkBox = (CheckBox) valueChangeEvent.getSource();
                        boolean checked = checkBox.getValue();
                        String text = checkBox.getText();
                        String searchAreaName = getValueByDisplayText(text);
                        if (checked) {
                            // выбираем область поиска
                            searchQuery.getAreas().add(searchAreaName);
                        } else {
                            // если область поиска "убрали" - удаляем ее из
                            // условий поиска
                            searchQuery.getAreas().remove(searchAreaName);
                        }

                        refreshDOTypesData();
                    }
                });

                searchAreaCheckBoxes.put(key, cb);
                AbsolutePanel cbDecorated = new AbsolutePanel();
                cbDecorated.setStyleName("srch-cb-decorated");
                searchAreass.add(cbDecorated);
                cbDecorated.add(cb);
            }

            for (int i = 0; i < areasDomainObjects.size(); i++) {
                // key - это группа(область поиска) к которой кнопка относится
                String domainObjectTypeName = areasDomainObjects.get(i);
                if (!targetDomainObjectsRadioButtons.containsKey(domainObjectTypeName)) {
                    rb = new RadioButton(domainObjectTypeName, getDisplayTextByValue(domainObjectTypeName));
                    rb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                            RadioButton radioButton = (RadioButton) valueChangeEvent.getSource();
                            boolean selected = radioButton.getValue();
                            if (selected) {
                                for (RadioButton rb : targetDomainObjectsRadioButtons.values()) {
                                    rb.setValue(false);
                                }
                                // выбираем целевой доменный объект для поиска
                                String targetObjectType = getValueByDisplayText(radioButton.getText());
                                searchQuery.setTargetObjectType(targetObjectType);
                                // вызываем форму поиска
                                invokeSearchForm(searchQuery, null);
                                // устанавливаем кнопку
                                radioButton.setValue(true);
                            }
                        }
                    });


                    targetDomainObjectsRadioButtons.put(domainObjectTypeName, rb);
                    /*
                     * AbsolutePanel rbDecorated = new AbsolutePanel();
                     * rbDecorated.setStyleName("srch-rb-decorated");
                     * rbDecorated.add(rb); domainObjects.add(rbDecorated);
                     */
                }
            }
        }

        AbsolutePanel cont = new AbsolutePanel();
        cont.add(searchAreass);
        cont.add(domainObjects);
        cont.add(scrollSearchForm);

        searchPanel.add(cont);
        container.add(searchPanel);

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

    public void setupFormSizes(int dialogHeight, int dialogWidth){
        searchPanel.setWidth(dialogWidth + "px");
        searchPanel.setHeight((dialogHeight - 100) + "px");
    }

    public void setSearchPopup(ExtSearchDialogBox extSearchDialogBox) {
        this.extSearchDialogBox = extSearchDialogBox;
        extSearchDialogBox.setUpDialogWindow(popupConfig);
    }

    // отправка данных в поисковый сервис
    public void sendSearchData(final ExtendedSearchData extendedSearchData) {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto resultSearchData) {
                // Window.alert(" Данные расширенного поиска ОТПРАВЛЕНЫ !");
                // удаляем окно поиска
                extSearchDialogBox.hide();
                Application.getInstance().getEventBus().fireEvent(
                        new ExtendedSearchCompleteEvent((ExtendedSearchDomainObjectSurfacePluginData)resultSearchData));
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

    // Вызов формы поиска, которая соответствует целевому доменному объекту и
    // всем условиям поиска
    private void invokeSearchForm(SearchQuery searchQuery, Map<String, WidgetState> formWidgetStates) {
        // удаляем прошлую форму поиска
        // if (extendSearchFormPluginPanel != null)
        // container.remove(extendSearchFormPluginPanel);
        /*
         * if (scrollSearchForm != null) container.remove(scrollSearchForm) ;
         */
        try {
            ExtendedSearchData extendedSearchData = new ExtendedSearchData();
            extendedSearchData.setSearchQuery(searchQuery);
            extendedSearchData.setFormWidgetsData(formWidgetStates);
            extendSearchFormPluginPanel = new PluginPanel();

            extendedSearchFormPlugin = ComponentRegistry.instance.get("extended.search.form.plugin");
            extendedSearchFormPlugin.setConfig(extendedSearchData);
            extendSearchFormPluginPanel.open(extendedSearchFormPlugin);
            extendSearchFormPluginPanel.setClassForPluginPanel("ext-search-content-wrapper");

            extendSearchFormPluginPanel.asWidget().getElement().getStyle().clearOverflow();
            scrollSearchForm.clear();
            scrollSearchForm.add(extendSearchFormPluginPanel);
        }catch (Exception e){
            throw new GuiException(e.getMessage(), e);
        }
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
        FormPanel panel = (FormPanel) extendedSearchFormPluginView.getViewWidget();

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


    private void refreshDOTypesData() {
        domainObjects.clear();
        HashSet<String> typesAvailable = new HashSet<>();
        for (String area : searchQuery.getAreas()) {
            typesAvailable.addAll(searchAreasData.get(area));
        }
        for (String type : typesAvailable) {
            AbsolutePanel cbDecorated = new AbsolutePanel();
            cbDecorated.setStyleName("srch-cb-decorated");
            domainObjects.add(cbDecorated);
            cbDecorated.add(targetDomainObjectsRadioButtons.get(type));
        }

        String currentType = searchQuery.getTargetObjectTypes().get(0);
        if (currentType != null && !typesAvailable.contains(currentType)) {
            searchQuery.setTargetObjectType(null);
            targetDomainObjectsRadioButtons.get(currentType).setValue(false);
            scrollSearchForm.clear();
        }
    }

    public void resetFormByInitData(Map<String, WidgetState> extendedSearchConfiguration, List<String> searchAreas, String domainObjectype) {
        // select SearchArea

        if(searchAreas != null){
            for(String searchArea : searchAreas){
                CheckBox searchAreaCh =  searchAreaCheckBoxes.get(searchArea);
                searchAreaCh.setValue(true, true);
            }

            // select search object

            RadioButton rb = targetDomainObjectsRadioButtons.get(domainObjectype);
            rb.setValue(true, false);

            // init form
            searchQuery.setTargetObjectType(domainObjectype);
            // вызываем форму поиска
            invokeSearchForm(searchQuery, extendedSearchConfiguration);
        }



    }
}
