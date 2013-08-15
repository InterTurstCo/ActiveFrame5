/*
 * Copyright 2011-2012 InterTrust LTD. All rights reserved. Visit our web-site: www.intertrust.ru.
 */
package ru.intertrust.cm.core.gui.impl.markup.cmj41.navigation.client.module.root.view.sidebar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * SidebarConstants
 * 
 * @author alex oreshkevich
 */
public interface SidebarConstants extends ConstantsWithLookup {

  SidebarConstants RESOURCES = GWT.create(SidebarConstants.class);

  String notifications();

  String tasks();

  String calendars();

  String documents();

  String cases();

  String discussions();

  String helpers();

  String reports();
}
