package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.TopPanelConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.CurrentUserInfo;
import ru.intertrust.cm.core.gui.impl.client.CurrentVersionInfo;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchShowDialogBoxEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchShowDialogBoxEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperLinkWithHistorySupport;
import ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch.ExtSearchDialogBox;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.PlainTextUserExtraInfo;
import ru.intertrust.cm.core.gui.model.UserExtraInfo;
import ru.intertrust.cm.core.gui.model.VersionInfo;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
import static ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager.*;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * Entry point classes define <code>createHeader()</code>
 */
public class HeaderContainer extends SimplePanel implements ExtendedSearchShowDialogBoxEventHandler {
  private FocusPanel thirdImage;
  public static final int FIRST_ROW = 0;
  private InformationDialogBox dialogBox;
  private SettingsPopupConfig settingsPopupConfig;
  private TopPanelConfig topPanelConfig;
  private PopupPanel popupPanel;
  private AbsolutePanel infoPanel;
  private ExtSearchDialogBox extendedSearchDialogBox;

  public HeaderContainer() {
  }

  public HeaderContainer(BusinessUniverseInitialization initialization) {
    this.settingsPopupConfig = initialization.getSettingsPopupConfig();
    this.topPanelConfig = initialization.getTopPanelConfig();
    CurrentUserInfo currentUserInfo = getUserInfo(initialization);
    addUserInfoToDialog(currentUserInfo);

    this.getElement().setId("container");
    this.getElement().getStyle().setProperty("position", "relative");

    SimplePanel head = createHeadPanel(this);
    String logoImagePath = GlobalThemesManager.getResourceFolder() + initialization.getLogoImagePath();
    FlexTable headTable = createHeadTable(logoImagePath);

    if ((topPanelConfig!= null && topPanelConfig.getNvisible() == true)
        || topPanelConfig == null) {
      headTable.setWidget(FIRST_ROW, 1, new HeaderSectionSuggestBox());
    }

    headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 1, "H_td_notes");

    final HyperLinkWithHistorySupport userRepresentation = new HyperLinkWithHistorySupport(buildUserRepresentation(currentUserInfo), "login");
    userRepresentation.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.show();
      }
    }, ClickEvent.getType());

    if (initialization.isSearchConfigured() && (((topPanelConfig != null && topPanelConfig.getSvisible() == true)
        || topPanelConfig == null))) {
      createSearchItem();
      headTable.setWidget(FIRST_ROW, 2, thirdImage);
    }
    headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 2, "H_td_ExtSearch");

    final UserExtraInfo userExtraInfo = initialization.getUserExtraInfo();
    String userExtraInfoText = userExtraInfo instanceof PlainTextUserExtraInfo ? ((PlainTextUserExtraInfo) userExtraInfo).getText() : "";
    final InlineLabel userPosition = new InlineLabel(userExtraInfoText);
    userPosition.addStyleName("HeadUserPost");

    final FlowPanel userInfoPanel = new FlowPanel();
    userInfoPanel.add(new SimplePanel(userRepresentation));
    userInfoPanel.add(new SimplePanel(userPosition));

    headTable.setWidget(FIRST_ROW, 3, userInfoPanel);
    headTable.getFlexCellFormatter().setStyleName(FIRST_ROW, 3, "H_td_user");

    final HorizontalPanel linksPanel = new HorizontalPanel();
    final AbsolutePanel decoratedSettings = new AbsolutePanel();
    decoratedSettings.setStyleName("decorated-settings");
    Image settingsImage = new Image(getCurrentTheme().settingsIm());
    Image versionImage = new Image(getCurrentTheme().helpIm());

    decoratedSettings.add(settingsImage);
    AbsolutePanel decoratedHelp = new AbsolutePanel();
    decoratedHelp.setStyleName("decorated-help");

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
    CurrentVersionInfo version = getVersion(initialization);
    if (initialization.getLoginScreenConfig() != null && initialization.getLoginScreenConfig().isDisplaycoreVersion() && version.getCoreVersion() != null) {
      infoPanel.add(new Label(LocalizeUtil.get(CORE_VERSION_KEY, CORE_VERSION) + " " + version.getCoreVersion()));
    }
    if (initialization.getLoginScreenConfig() != null && initialization.getLoginScreenConfig().isDisplayProductVersion() && version.getProductVersion() != null) {
      infoPanel.add(new Label(LocalizeUtil.get(VERSION_KEY, VERSION) + " " + version.getProductVersion()));
    }
    if (initialization.getLoginScreenConfig() != null && initialization.getLoginScreenConfig().isDisplayVersionList() && initialization.getProductVersionList() != null) {
      for (VersionInfo productVersion : initialization.getProductVersionList()) {
        infoPanel.add(new Label(productVersion.getComponent() + " : " + productVersion.getVersion()));
      }
    }

    popupPanel.add(contentInfo);

    popupPanel.getElement().setClassName("applicationVersionWindows");

    addHelpItemClickHandler(decoratedHelp, versionImage);

    linksPanel.add(decoratedSettings);
    linksPanel.add(decoratedHelp);

    headTable.setWidget(FIRST_ROW, 4, linksPanel);
    headTable.getCellFormatter().setStyleName(FIRST_ROW, 4, "H_td_links");
    HyperLinkWithHistorySupport logoutLink = new HyperLinkWithHistorySupport(LocalizeUtil.get(EXIT_KEY, EXIT), "logout");
    headTable.setWidget(FIRST_ROW, 5, logoutLink);
    addSettingsClickHandler(decoratedSettings);

    logoutLink.addDomHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        logout();
      }
    }, ClickEvent.getType());
    headTable.getCellFormatter().setStyleName(FIRST_ROW, 5, "H_td_logout");
    head.add(headTable);
    setInfoPage(initialization.getHelperLink());
  }

  @Deprecated
  public HeaderContainer(CurrentUserInfo currentUserInfo, String logoImagePath, final SettingsPopupConfig settingsPopupConfig,
                         CurrentVersionInfo version, String helperLink) {

  }

  private void createSearchItem() {
    thirdImage = new FocusPanel();
    thirdImage.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().headerExtendedSearch());
    thirdImage.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showExtendedSearchPanel(true, null, null, null);
      }
    }, ClickEvent.getType());
  }

  private void addHelpItemClickHandler(AbsolutePanel decoratedHelp, final Image versionImage) {
    decoratedHelp.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        if (!popupPanel.isShowing()) {
          popupPanel.showRelativeTo(versionImage);
          popupPanel.getElement().getStyle().clearLeft();
          popupPanel.getElement().getStyle().clearTop();

        } else {
          popupPanel.hide();
        }
      }
    }, ClickEvent.getType());
  }

  private void addSettingsClickHandler(AbsolutePanel decoratedSettings) {
    decoratedSettings.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<String> themeNames = new ArrayList<String>();
        themeNames.add(THEME_DARK);
        themeNames.add(THEME_DEFAULT);
        SettingsPopup settingsPopup = new SettingsPopup(settingsPopupConfig);
        settingsPopup.show();
        settingsPopup.getElement().getStyle().clearLeft();
      }
    }, ClickEvent.getType());
  }

  // вызываем окно расширенного поиска
  public void showExtendedSearchPanel(boolean isNew, Map<String, WidgetState> extendedSearchConfiguration, List<String> searchAreas, String searchDomainObjectType) {

    if (isNew || extendedSearchDialogBox == null) {
      extendedSearchDialogBox = new ExtSearchDialogBox();
      extendedSearchDialogBox.setModal(true);
      extendedSearchDialogBox.setGlassEnabled(true);
      extendedSearchDialogBox.setPopupPosition(thirdImage.getAbsoluteLeft() - 545, 45);
    }

    extendedSearchDialogBox.setInitedFormData(extendedSearchConfiguration, searchAreas, searchDomainObjectType);
    extendedSearchDialogBox.show();
  }

  @Override
  public void onShowExtendedSearchDialog(ExtendedSearchShowDialogBoxEvent event) {
    showExtendedSearchPanel(event.isNewDialog(), event.getExtendedSearchConfiguration(), event.getSearchAreas(), event.getSearchDomainObjectType());
  }

  private void logout() {

    AsyncCallback<Void> callback = new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        if (!Window.Location.getPath().contains("BusinessUniverse.html")) {

          Window.Location.assign(GWT.getHostPageBaseURL() +
              Window.Location.getPath().substring(Window.Location.getPath().lastIndexOf("/") + 1) +
              Window.Location.getQueryString());
          Window.Location.reload();
        } else {

          Window.Location.assign(GWT.getHostPageBaseURL() +
              BusinessUniverseConstants.LOGIN_PAGE +
              Window.Location.getQueryString());
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.LOGOUT_ERROR_MESSAGE_KEY,
            BusinessUniverseConstants.LOGOUT_ERROR_MESSAGE));
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

  private void addUserInfoToDialog(CurrentUserInfo currentUserInfo) {
    dialogBox = new InformationDialogBox(currentUserInfo.getFirstName(), currentUserInfo.getLastName(),
        currentUserInfo.getCurrentLogin(), currentUserInfo.getMail());
  }

  private void setInfoPage(final String pagePath) {

    final String currentPath = (pagePath.contains("http://") || pagePath.contains("https://")) ? pagePath : GWT.getHostPageBaseURL() + pagePath;
    Label help = new Label(LocalizeUtil.get(INFO_KEY, INFO));
    infoPanel.add(help);

    help.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open(currentPath, "_blank", "");
      }
    });
  }

  CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
    return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
  }

  CurrentVersionInfo getVersion(BusinessUniverseInitialization result) {
    return new CurrentVersionInfo(result.getApplicationVersion(), result.getProductVersion());
  }

  private String buildUserRepresentation(CurrentUserInfo currentUserInfo) {
    StringBuilder sb = new StringBuilder();
    if (currentUserInfo.getFirstName().isEmpty() && currentUserInfo.getLastName().isEmpty()) {
      sb.append(currentUserInfo.getCurrentLogin());
    } else {
      sb.append(currentUserInfo.getFirstName());
      sb.append(" ");
      sb.append(currentUserInfo.getLastName());
    }
    return sb.toString();
  }
}
