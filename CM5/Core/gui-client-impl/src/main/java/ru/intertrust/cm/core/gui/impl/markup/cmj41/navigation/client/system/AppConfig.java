/*
 * Copyright 2011-2012 InterTrust LTD. All rights reserved. Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.system;

import com.google.gwt.core.client.GWT;

/**
 * AppConfig <br/>
 * Common constants for application. <br>
 * We do not use server-side gwt feautures in this project, this is why we must have client-side constants.
 * 
 * @author alex oreshkevich
 */
public interface AppConfig {

  /** Browser title */
  String  BROWSER_TITLE           = "cmj4";

  String  LOADING_TITLE_ID        = "loading_title";

  /** Определяет использовать тестовые (true) сереверные данные или реальные (false) */
  boolean IS_MOCK_SERVICE_ALLOWED = false;

  boolean IS_PROD_MODE            = GWT.isProdMode();

  int     AUTH_TIMEOUT            = 10000;

  String  AUTH_COOKIE             = "CMJ-Auth";

  String  AUTH_CL_COOKIE          = "CMJ-Auth-Client";

  String  AUTH_CL_COOKIE_TIMER    = "CMJ-Auth-Client-Timer";

  int     SIDEBAR_ANIMATION       = 500;
}
