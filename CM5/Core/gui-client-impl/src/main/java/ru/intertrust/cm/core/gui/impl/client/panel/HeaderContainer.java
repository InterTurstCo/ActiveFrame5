package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.CurrentUserInfo;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch.ExtendedSearchPlugin;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

/**
 * Entry point classes define <code>createHeader()</code>
 */
public class HeaderContainer extends SimplePanel {

    public static final int FIRST_ROW = 0;
    InformationDialogBox dialogBox;
    private int centralPluginWidth;
    private int entralPluginHeight;
    private PluginPanel extendSearchPluginPanel;
    private ExtendedSearchPlugin extendedSearchPlugin;

    public HeaderContainer(CurrentUserInfo currentUserInfo, String logoImagePath) {
        addUserInfoToDialog(currentUserInfo);

        this.getElement().setId("container");
        this.getElement().getStyle().setProperty("position", "relative");

        SimplePanel head = createHeadPanel(this);
        FlexTable headTable = createHeadTable(logoImagePath);

        headTable.setWidget(FIRST_ROW, 1, new HeaderSectionSuggestBox());
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 1, "H_td_notes");

        Hyperlink userName = new Hyperlink(currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName(), "login");
        userName.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                 dialogBox.show();
            }
        });

        final FocusPanel thirdImage = new FocusPanel();
        thirdImage.setStyleName("header-third-action-button");
        thirdImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getExtendedSearchPanel();
            }
        });
        headTable.setWidget(FIRST_ROW, 2, thirdImage);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 2, "H_td_ExtSearch");


        InlineLabel userPosition = new InlineLabel("Администратор");
        userPosition.addStyleName("HeadUserPost");

        FlowPanel userInfoPanel = new FlowPanel();
        userInfoPanel.add(new SimplePanel(userName));
        userInfoPanel.add(new SimplePanel(userPosition));

        headTable.setWidget(FIRST_ROW, 3, userInfoPanel);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 3, "H_td_user");

        HorizontalPanel linksPanel = new HorizontalPanel();
        AbsolutePanel decoratedSettings = new AbsolutePanel();
        decoratedSettings.setStyleName("decorated-settings");
        Anchor settings = new Anchor("Settings", "Settings");
        decoratedSettings.add(settings);

        AbsolutePanel decoratedHelp = new AbsolutePanel();
        decoratedHelp.setStyleName("decorated-help");
        Anchor help = new Anchor("Help", "Help");
        decoratedHelp.add(help);

        linksPanel.add(decoratedSettings);
        linksPanel.add(decoratedHelp);

        headTable.setWidget(FIRST_ROW, 4, linksPanel);
        headTable.getCellFormatter().setStyleName(FIRST_ROW, 4, "H_td_links");
        Hyperlink logoutLink = new Hyperlink("Выход", "logout");
        headTable.setWidget(FIRST_ROW, 5, logoutLink);

        logoutLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
               logout();
            }
        });
        headTable.getCellFormatter().setStyleName(FIRST_ROW, 5, "H_td_logout");
        head.add(headTable);
    }

    // вызываем окно расширенного поиска
    private void getExtendedSearchPanel() {
        extendSearchPluginPanel = new PluginPanel();
        extendedSearchPlugin = ComponentRegistry.instance.get("extended.search.plugin");

        extendSearchPluginPanel.open(extendedSearchPlugin);
        extendSearchPluginPanel.setSize("800px", "400px");

        PopupPanel popupPanel = new PopupPanel();
        popupPanel.isAnimationEnabled();
        popupPanel.add(extendSearchPluginPanel);
        popupPanel.center();
        popupPanel.setSize("800px", "400px");
        popupPanel.show();
        popupPanel.getElement().getStyle().setZIndex(9);
        popupPanel.getElement().setAttribute("id", "search-popup");

    }

    private void logout(){
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Window.Location.assign("/cm-sochi/Login.html"+ Window.Location.getQueryString());
            }
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error logout!");
            }
        };
        BusinessUniverseAuthenticationServiceAsync.Impl.getInstance().logout(callback);
    }

    private FlexTable createHeadTable(String logoImagePath) {
        FlexTable headTable = new FlexTable();
        HTMLTable.CellFormatter formatter = headTable.getCellFormatter();
        formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        formatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        headTable.addStyleName("HeadTable");
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 0, "H_td_logo");
        AbsolutePanel imageContainer = new AbsolutePanel();
        imageContainer.addStyleName("header-logo-container");
        imageContainer.add(new Image(logoImagePath));
        headTable.setWidget(FIRST_ROW, 0, imageContainer);
        return headTable;
    }

    private SimplePanel createHeadPanel(SimplePanel container) {
        SimplePanel head = new SimplePanel();
        container.add(head);
        head.getElement().setId("Head");
        return head;
    }

    public void addUserInfoToDialog(CurrentUserInfo currentUserInfo){
        dialogBox = new InformationDialogBox(currentUserInfo.getCurrentLogin(), currentUserInfo.getFirstName(), currentUserInfo.getLastName(), currentUserInfo.getMail());
    }

}
