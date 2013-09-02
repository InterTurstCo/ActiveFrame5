package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.mainlayout;

import java.util.ArrayList;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ContainerPanel extends FlexTable {

    private final AbsolutePanel dragPanel = new AbsolutePanel();
    private final FlowPanel selectedPanel = new FlowPanel();
    private final Button btnShowPopup;
    private final Label btnClear = new Label();
    private final TextBox searchField = new TextBox();
    private final FlexTable searchPanel = new FlexTable();
    // изображение уронившегося дауна
    // private final Image dropDown = new Image(ImgRes.I.triangleImg());

    private FlexCellFormatter flexFormatter;
    private final PopupPanel panelSearch;
    private Integer peopleShow = -1;
    private Boolean showSearch = false;
    private final ArrayList<Timer> ts = new ArrayList<Timer>();
    private Timer t = null;
    private ModeShow modeShow = ModeShow.noneSlot;
    private Boolean columnShow = false;
    private HandlerRegistration nativePreviewHandlerRegistration;
    private HandlerRegistration globalHandlerRegistration;
    private String secondaryStyle = "";
    private Boolean prefixOrgName = false;
    private Boolean postfixWorkPhone = false;
    private Boolean postfixAllPhones = false;
    private Boolean postfixRegistratorName = false;
    private boolean fullGetData = false;
    private String titleDialog = "";
    private HandlerRegistration handlerSelect = null;
    private final InlineLabel emptyContainerText = new InlineLabel();
    private final InlineLabel privatePersonContainerText = new InlineLabel();
    private boolean privatePersonBeforeName = false;
    private boolean boldOrgName = false;
    private boolean showCorrespondentLabel;
    private boolean searchFromSelected = false;
    private boolean keyInField = false;
    private boolean showFocusFirst = true;
    private boolean isNoSearchForAdress;
    private String scWidth = "0px";

    private enum ModeShow {
        documentSidesSlot,
        searchSlot,
        loadingSlot,
        noneSlot;
    }

    public ContainerPanel() {
        super();
        btnShowPopup = new Button() {
            @Override
            public void setTabIndex(final int index) {
                super.setTabIndex(-1);
            };
        };

        btnShowPopup.setTabIndex(-1);
        panelSearch = new PopupPanel(true) {
            @Override
            public void hide() {
                if (super.isShowing() && !keyInField) {
                    searchField.setText("");
                }
                super.hide();
            }
        };

        setStyles();
        setCorrespondentLabel(false);

        privatePersonContainerText.setStyleName("Bold_R_S_item");

        emptyContainerText.setStyleName("Grayed_R_S_item");

        dragPanel.clear();

        dragPanel.add(searchPanel);

        initButtonsPanel();
    }

    private VerticalPanel initButtonsPanel() {

        VerticalPanel vp = new VerticalPanel();
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        vp.getElement().getStyle().setMarginLeft(8, Unit.PX);
        vp.add(btnShowPopup);
        vp.add(btnClear);

        Image btnNavigation1 = new Image("images/inbox.png");
        Image btnNavigation2 = new Image("images/tasks.png");
        Image btnNavigation3 = new Image("images/calendar.png");
        Image btnNavigation4 = new Image("images/docs.png");
        Image btnNavigation5 = new Image("images/cases.png");
        Image btnNavigation6 = new Image("images/helpers.png");
        Image btnNavigation7 = new Image("images/analitika.png");

        vp.add(btnNavigation1);
        vp.add(btnNavigation2);
        vp.add(btnNavigation3);
        vp.add(btnNavigation4);
        vp.add(btnNavigation5);
        vp.add(btnNavigation6);
        vp.add(btnNavigation7);

        setWidget(0, 1, vp);
        flexFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);

        // vp.setVisible(false);
        vp.setVisible(true);
        btnShowPopup.setVisible(false);
        btnClear.setVisible(false);
        return vp;
    }

    public TextBox getSearchField() {
        return searchField;
    }

    public FlowPanel getSelectedPanel() {
        return selectedPanel;
    }

    public AbsolutePanel getDragPanel() {
        return dragPanel;
    }

    public void setPrefixOrgName(final boolean prefixOrgName) {
        this.prefixOrgName = prefixOrgName;
    }

    public void setPostfixWorkPhone(final boolean postfixWorkPhone) {
        this.postfixWorkPhone = postfixWorkPhone;
    }

    public void setPostfixAllPhones(final boolean postfixAllPhones) {
        this.postfixAllPhones = postfixAllPhones;
    }

    public void setPostfixRegistratorName(final boolean postfixRegistratorName) {
        this.postfixRegistratorName = postfixRegistratorName;
    }

    public void setIsNoSearchForAdress(boolean isNoSearchForAdress) {
        this.isNoSearchForAdress = isNoSearchForAdress;
        searchField.setReadOnly(true);
    }

    public boolean hasIsNoSearchForAdress() {
        return isNoSearchForAdress;
    }

    private void setWidgets(final boolean showCorrespondentLabel) {
        if (isCellPresent(0, 0)) {
            if (getWidget(0, 0) != null) {
                clearCell(0, 0);
            }
        }
    }

    private void setStyles() {
        // dropDown.setStyleName("button");
        // dropDown.setVisible(false);
        searchPanel.setWidget(0, 0, selectedPanel);
        // searchPanel.setWidget(0, 1, dropDown);
        searchPanel.getFlexCellFormatter().setWidth(0, 0, "100%");
        searchPanel.getFlexCellFormatter().setWidth(0, 1, "30px");
        searchPanel.getFlexCellFormatter().setStyleName(0, 1, "button");

        // selectedPanel.setStyleName("R_S");
        // selectedPanel.setStyleName(DetailsTab.style.R_S());
        flexFormatter = getFlexCellFormatter();
        // listBoxPanel.setHeight("125px");
        setCellSpacing(0);
        setCellPadding(0);
        btnShowPopup.setStyleName("As_button As_li_add");
        // loadingPanel.setStyleName("slot");
        // setButtonText(presenterData.getI18nBridge().getString(FunctionName.DIALOG_CHOOSE_BTN));
        // btnClear.setStyleName(DetailsTab.style.clearLabel());
        panelSearch.setStyleName("container-panel-slot");
    }

    public void setButtonText(final String text) {
        btnShowPopup.setHTML("<i></i>" + text);
    }

    private void setFocusElement() {
        showFocusFirst = false;
        searchField.setFocus(true);
        if (peopleShow == 1) {
            onFullSelectData();
        }
    }

    public void onFullSelectData() {

    }

    public void showButton(final boolean showButton) {
        if (showButton) {
            btnShowPopup.getParent().setVisible(true);
        }
        btnShowPopup.setVisible(showButton);
        // flexFormatter.getElement(0, 1).getStyle().setPaddingLeft(5, Unit.PX);
        // flexFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        // flexFormatter.setRowSpan(0, 1, 2);
    }

    public void showClearButton(final boolean showClearButton) {
        if (showClearButton) {
            btnClear.getParent().setVisible(true);
        }
        btnClear.setVisible(showClearButton);
        // flexFormatter.getElement(0, 2).getStyle().setPaddingLeft(5, Unit.PX);
        // flexFormatter.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
        // flexFormatter.setRowSpan(0, 2, 2);
    }

    public void setEmptyText(final String emptyText) {
        emptyContainerText.setText(emptyText);
    }

    public void setStyleNameContainer(final String style) {
        dragPanel.setStyleName(style);
    }

    public void setWidthContainer(final String width) {
        scWidth = width;
        // flexFormatter.setWidth(0, 0, width);
        dragPanel.setWidth(width);

    }

    public String getWidthContainer() {
        return scWidth;
    }

    public Integer getPeopleShow() {
        return peopleShow;
    }

    public void setPeopleShow(final Integer peopleShow) {
        this.peopleShow = peopleShow;
    }

    private void showSlot(final ModeShow show) {
        modeShow = show;
        switch (show) {
            case documentSidesSlot:
                panelSearch.clear();
                // panelSearch.add(documentSidesPanel);
                showPanelSearch();
                break;
            case noneSlot:
                panelSearch.hide();
                break;
            case loadingSlot:
                panelSearch.clear();
                // panelSearch.add(loadingPanel);
                showPanelSearch();
                break;
            case searchSlot:
                panelSearch.clear();
                // panelSearch.add(listBoxPanel);
                showPanelSearch();
                break;
            default:
                break;
        }
        updateHandlers();
    }

    public void setColumnShow(final boolean columnShow) {
        this.columnShow = columnShow;
    }

    public void addStyleForSelectedPeople(final String style) {
        secondaryStyle = style;
    }

    private void updateHandlers() {
        if (nativePreviewHandlerRegistration != null) {
            nativePreviewHandlerRegistration.removeHandler();
            nativePreviewHandlerRegistration = null;
        }
        if ((modeShow == ModeShow.loadingSlot) || (modeShow == ModeShow.searchSlot)) {
            nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
                @Override
                public void onPreviewNativeEvent(final NativePreviewEvent event) {
                    previewNativeEvent(event);
                }
            });
        }
    }

    private boolean eventTargetsPopup(final NativeEvent event) {
        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return getElement().isOrHasChild(Element.as(target));
        }
        return false;
    }

    private boolean eventTargetsPopupSearch(final NativeEvent event) {
        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return panelSearch.getElement().isOrHasChild(Element.as(target));
        }
        return false;
    }

    private void previewNativeEventGlobal(final NativePreviewEvent event) {
        Event nativeEvent = Event.as(event.getNativeEvent());
        boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent)
                || eventTargetsPopupSearch(nativeEvent);
        if (eventTargetsPopupOrPartner) {
            event.consume();
        }
        if (!eventTargetsPopupOrPartner) {
            unRegisterShowDialog();
        }
        else {
            registeShowDialog();
        }
    }

    private void previewNativeEvent(final NativePreviewEvent event) {
        Event nativeEvent = Event.as(event.getNativeEvent());
        boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent)
                || eventTargetsPopupSearch(nativeEvent);
        if (eventTargetsPopupOrPartner) {
            event.consume();
        }
        int type = nativeEvent.getTypeInt();
        if ((type != Event.ONMOUSEMOVE) && (type != Event.ONMOUSEOUT) && (type != Event.ONMOUSEOVER)
                && !eventTargetsPopupOrPartner) {
            showSlot(ModeShow.noneSlot);
            return;
        }
    }

    public void setOpenId(final String docTypeOpenid) {
        getElement().setId(docTypeOpenid);
    }

    public void setTitleDialog(final String title) {
        titleDialog = title;
    }

    private void showPanelSearch() {
        Integer left = getElement().getAbsoluteLeft();
        Integer top = getElement().getAbsoluteBottom();
        Integer width = getCellFormatter().getElement(0, 0).getOffsetWidth() - 20;
        panelSearch.setWidth(width.toString() + "px");
        panelSearch.setPopupPosition(left, top);
        panelSearch.show();
    }

    public void registeShowDialog() {
        unRegisterShowDialog();
        handlerSelect = Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(final NativePreviewEvent event) {
                NativeEvent ne = event.getNativeEvent();
                if (ne.getAltKey() && ((ne.getKeyCode() == 'D') || (ne.getKeyCode() == 'd'))) {
                    ne.preventDefault();
                    ne.stopPropagation();
                    // showDialog();
                }
            }
        });
    }

    public void unRegisterShowDialog() {
        if (handlerSelect != null) {
            handlerSelect.removeHandler();
            handlerSelect = null;
        }
    }

    public void setBoldOrgName(final boolean boldOrgName) {
        this.boldOrgName = boldOrgName;
    }

    protected void setPrivatePersonBeforeName(final boolean privatePersonBeforeName) {
        this.privatePersonBeforeName = privatePersonBeforeName;
    }

    public void setCorrespondentLabel(final boolean showCorrespondentLabel) {
        this.showCorrespondentLabel = showCorrespondentLabel;
        setWidgets(showCorrespondentLabel);
    }

    public boolean getCorrespondentLabel() {
        return showCorrespondentLabel;
    }

    public void setFullGetData(final boolean fullGetData) {
        this.fullGetData = fullGetData;
    }

    public void setFocus() {
        searchField.setFocus(true);
    }

    public void adjustSearchField() {
        int containerWidth = selectedPanel.getElement().getClientWidth();
        int sumWidths = 0;
        for (int i = 0; i < selectedPanel.getWidgetCount(); i++) {
            Widget w = selectedPanel.getWidget(i);
            if (w != searchField) {
                sumWidths += w.getOffsetWidth();
            }
        }

        final int MIN_SEARCH_FIELD_WIDTH = 100;
        int searchPanelWidth = (containerWidth - sumWidths) - 30; // 30 - for margins
        if (searchPanelWidth < MIN_SEARCH_FIELD_WIDTH) {
            searchField.setWidth(MIN_SEARCH_FIELD_WIDTH + "px");
        }
        else {
            searchField.setWidth(searchPanelWidth + "px");
        }
    }
}