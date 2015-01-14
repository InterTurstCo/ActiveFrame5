package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.CurrentUserInfo;
import ru.intertrust.cm.core.gui.impl.client.CurrentVersionInfo;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperLinkWithHistorySupport;
import ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch.ExtSearchDialogBox;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager.*;

/**
 * Entry point classes define <code>createHeader()</code>
 */
public class HeaderContainer extends SimplePanel {
    private FocusPanel thirdImage;
    public static final int FIRST_ROW = 0;
    private InformationDialogBox dialogBox;
    private SettingsPopupConfig settingsPopupConfig;
    private PopupPanel popupPanel;
    private Label platformVersion;
    private Label productVersion;
    private AbsolutePanel infoPanel;

    public HeaderContainer() {
    }

    public HeaderContainer(CurrentUserInfo currentUserInfo, String logoImagePath, final SettingsPopupConfig settingsPopupConfig, CurrentVersionInfo version) {
        this.settingsPopupConfig = settingsPopupConfig;
        addUserInfoToDialog(currentUserInfo);

        this.getElement().setId("container");
        this.getElement().getStyle().setProperty("position", "relative");

        SimplePanel head = createHeadPanel(this);
        FlexTable headTable = createHeadTable(logoImagePath);

        headTable.setWidget(FIRST_ROW, 1, new HeaderSectionSuggestBox());
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 1, "H_td_notes");

        final HyperLinkWithHistorySupport userName = new HyperLinkWithHistorySupport(currentUserInfo.getFirstName() + " " + currentUserInfo.getLastName(), "login");
        userName.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.show();
            }
        });

        thirdImage = new FocusPanel();
        thirdImage.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().headerExtendedSearch());
        thirdImage.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getExtendedSearchPanel();
            }
        }, ClickEvent.getType());

        headTable.setWidget(FIRST_ROW, 2, thirdImage);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 2, "H_td_ExtSearch");


        final InlineLabel userPosition = new InlineLabel("Администратор");
        userPosition.addStyleName("HeadUserPost");

        final FlowPanel userInfoPanel = new FlowPanel();
        userInfoPanel.add(new SimplePanel(userName));
        userInfoPanel.add(new SimplePanel(userPosition));

        headTable.setWidget(FIRST_ROW, 3, userInfoPanel);
        headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 3, "H_td_user");

        final HorizontalPanel linksPanel = new HorizontalPanel();
        final AbsolutePanel decoratedSettings = new AbsolutePanel();
        final Image settingsImage = new Image(getCurrentTheme().settingsIm());
        final Image versionImage = new Image("css/images/help.png");

        decoratedSettings.add(settingsImage);
        AbsolutePanel decoratedHelp = new AbsolutePanel();
        decoratedHelp.setStyleName("decorated-help");
        //Anchor help = new Anchor("Help", "Help");
        //decoratedHelp.add(help);
        decoratedHelp.add(versionImage);

        popupPanel = new PopupPanel(true, false);
        infoPanel = new AbsolutePanel();
        AbsolutePanel corner = new AbsolutePanel();
        corner.setStyleName("srch-corner");
        corner.getElement().getStyle().clearPosition();
        AbsolutePanel contentInfo = new AbsolutePanel();
        contentInfo.setStyleName("popupWrapper");
        contentInfo.getElement().getStyle().clearOverflow();
        contentInfo.add(corner);
        contentInfo.add(infoPanel);
        infoPanel.setStyleName("info-panel");
        infoPanel.add(new Label("Версия платформы: " + version.getCoreVersion()));
        if (version.getProductVersion() != null) {
            infoPanel.add(new Label("          Версия: " + version.getProductVersion()));
        }
        popupPanel.add(contentInfo);

        popupPanel.getElement().setClassName("applicationVersionWindows");

//        popupPanel.getElement().getStyle().setRight(72, Style.Unit.PX);
//        popupPanel.getElement().getStyle().setTop(55, Style.Unit.PX);

        versionImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {

                if (!popupPanel.isShowing()) {
                    popupPanel.showRelativeTo(versionImage);
                    //popupPanel.show();
                    popupPanel.getElement().getStyle().clearLeft();
                    popupPanel.getElement().getStyle().clearTop();

                } else {
                    popupPanel.hide();
                }
            }
        });

        linksPanel.add(decoratedSettings);
        linksPanel.add(decoratedHelp);

        headTable.setWidget(FIRST_ROW, 4, linksPanel);
        headTable.getCellFormatter().setStyleName(FIRST_ROW, 4, "H_td_links");
        HyperLinkWithHistorySupport logoutLink = new HyperLinkWithHistorySupport("Выход", "logout");
        headTable.setWidget(FIRST_ROW, 5, logoutLink);
        settingsImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<String> themeNames = new ArrayList<String>();
                themeNames.add(THEME_DARK);
                themeNames.add(THEME_DEFAULT);
                SettingsPopup settingsPopup = new SettingsPopup(settingsPopupConfig);
                settingsPopup.show();
                settingsPopup.getElement().getStyle().clearLeft();
            }
        });
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

        ExtSearchDialogBox dialog = new ExtSearchDialogBox();
        dialog.setModal(true);
        dialog.setGlassEnabled(true);
        dialog.setPopupPosition(thirdImage.getAbsoluteLeft() - 545, 45);
        dialog.show();
    }

    private void logout() {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Window.Location.assign(GWT.getHostPageBaseURL() + BusinessUniverseConstants.LOGIN_PAGE +
                        Window.Location.getQueryString());
            }

            @Override
            public void onFailure(Throwable caught) {
                ApplicationWindow.errorAlert(BusinessUniverseConstants.LOGOUT_ERROR_MESSAGE);
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

    public void addUserInfoToDialog(CurrentUserInfo currentUserInfo) {
        dialogBox = new InformationDialogBox(currentUserInfo.getFirstName(), currentUserInfo.getLastName(),
                currentUserInfo.getCurrentLogin(),currentUserInfo.getMail());
    }

    public void setInfoPage(final String pagePath){

       final String currentPath = pagePath.contains("http://")? pagePath : GWT.getHostPageBaseURL()+ pagePath;
        Label help = new Label("Справка");
        infoPanel.add(help);

        help.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    Window.open(currentPath, "_blank", "");
            }
        });
    }

}
