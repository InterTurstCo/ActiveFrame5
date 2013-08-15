package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.system;

//import com.gwtplatform.mvp.client.annotations.NameToken;
//import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * The central location of all name tokens for the application. All {@link ProxyPlace} classes get their tokens from
 * here. This class also makes it easy to use name tokens as a resource within UIBinder xml files.
 * <p />
 * The public static final String is used within the annotation {@link NameToken}, which can't use a method and the
 * method associated with this field is used within UiBinder which can't access static fields.
 * <p />
 * Also note the exclamation mark in front of the tokens, this is used for search engine crawling support.
 * 
 * @author alex oreshkevich
 */
public class NameTokens {

  // / temp

  public static final String localDialogSamplePage  = "!localDialogPage";
  public static final String globalDialogSamplePage = "!globalDialogPage";
  public static final String adminPage              = "!adminPage";
  public static final String homeNewsPage           = "!homeNewsPage";
  public static final String homeInfoPage           = "!homeInfoPage";
  public static final String settingsPage           = "!settingsPage";

  public static String getLocalDialogSamplePage() {
    return localDialogSamplePage;
  }

  public static String getGlobalDialogSamplePage() {
    return globalDialogSamplePage;
  }

  public static String getAdminPage() {
    return adminPage;
  }

  public static String getHomeNewsPage() {
    return homeNewsPage;
  }

  public static String getHomeInfoPage() {
    return homeInfoPage;
  }

  public static String getSettingsPage() {
    return settingsPage;
  }

  // //

  public static final String application       = "application";

  /** content содержит: список (DynamicGrid), показывающий некоторую коллекцию; преьвю листа данной коллекции. */
  public static final String content           = "content";

  /** Уведомления - http://intertrust.dev.design.ru/inbox/ */
  public static final String notificationsPage = "notifications";

  /** Задания - http://intertrust.dev.design.ru/tasks/ */
  public static final String tasksPage         = "tasks";

  /** План работ - http://intertrust.dev.design.ru/calendar2/ */
  public static final String calendarPage      = "plans";

  /** Документы - http://intertrust.dev.design.ru/docs/ */
  public static final String docsPage          = "documents";

  /** Обсуждения - http://intertrust.dev.design.ru/discussions/ */
  public static final String discussionsPage   = "discussions";

  /** Справочники - http://intertrust.dev.design.ru/helpers/ */
  public static final String helpersPage       = "helpers";

  /** Справочники - http://intertrust.dev.design.ru/helpers/ */
  public static final String emptyPage         = "empty";

  /** Кейсы - http://intertrust.dev.design.ru/cases/ */
  public static final String casesPage         = "cases";

  /** Analitycs - http://intertrust.dev.design.ru/analitika/ */
  public static final String reportsPage       = "reports";

  /** Состояние без загрузки контента */
  public static final String homePage          = "index";

  /** Состояние без загрузки контента */
  public static final String patternDocument   = "pattern";

  /** Любой ошибочный токен */
  public static final String errorPage         = "error";

  public static final String auth              = "auth";

  public static final String sticker           = "sticker";

  public static final String locker            = "locker";

  public static final String help              = "help";

  public static String getCalendarpage() {
    return calendarPage;
  }

  public static String getCasespage() {
    return casesPage;
  }

  public static String getDiscussionspage() {
    return discussionsPage;
  }

  public static String getDocspage() {
    return docsPage;
  }

  public static String getHelperspage() {
    return emptyPage;
  }

  public static String getHomepage() {
    return homePage;
  }

  public static String getInboxpage() {
    return notificationsPage;
  }

  public static String getPatterndocument() {
    return patternDocument;
  }

  public static String getTaskspage() {
    return tasksPage;
  }

  public static String getHelp() {
    return help;
  }

}
