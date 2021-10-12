package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 24.09.14
 *         Time: 20:02
 */
public class LinkUtil {
  private static final String BC_SHORT_SUFFIX = "...";

  private LinkUtil() {
  } // non-instantiable

  public static void markNavigationHierarchy(List<LinkConfig> linkConfigList, LinkConfig parent, ChildLinksConfig parentChildLinksConfig) {
    for (LinkConfig linkConfig : linkConfigList) {
      linkConfig.setParentLinkConfig(parent);
      linkConfig.setParentChildLinksConfig(parentChildLinksConfig);
      for (ChildLinksConfig childLinksConfig : linkConfig.getChildLinksConfigList()) {
        markNavigationHierarchy(childLinksConfig.getLinkConfigList(), linkConfig, childLinksConfig);
      }
    }
  }


  public static void findLink(String link, List<LinkConfig> linkConfigList, List<LinkConfig> results) {
    for (LinkConfig linkConfig : linkConfigList) {
      if (linkConfig.getName().equals(link)) {
        results.add(linkConfig);
        return;
      }
      for (ChildLinksConfig childLinksConfig : linkConfig.getChildLinksConfigList()) {
        findLink(link, childLinksConfig.getLinkConfigList(), results);
      }
    }
  }

  public static void addHierarchicalLinkToNavigationConfig(NavigationConfig navigationConfig, LinkConfig link) {
    LinkConfig parentLink = findParentLink(navigationConfig, link);
    link.setParentLinkConfig(parentLink);
    if (!navigationConfig.getHierarchicalLinkList().contains(link)) {
      navigationConfig.getHierarchicalLinkList().add(link);
    }
  }

  private static LinkConfig findParentLink(NavigationConfig navigationConfig, LinkConfig link) {
    LinkConfig parentLinkConfig = null;
    HistoryManager manager = Application.getInstance().getHistoryManager();
    String parentLinkName = manager.getLink();
    List<LinkConfig> results = new ArrayList<>();
    LinkUtil.findLink(parentLinkName, navigationConfig.getLinkConfigList(), results);
    if (results.isEmpty()) {
      LinkUtil.findLink(parentLinkName, navigationConfig.getHierarchicalLinkList(), results);
    }
    if (!results.isEmpty()) {
      parentLinkConfig = results.get(0);
    }
    return parentLinkConfig;
  }

  public static FormViewerConfig findHierarchyRootFormViewerConfig(NavigationConfig navigationConfig) {
    List<LinkConfig> linkConfigs = navigationConfig.getHierarchicalLinkList();
    LinkConfig parentLinkConfig = linkConfigs.isEmpty() ? null : linkConfigs.get(0);
    parentLinkConfig = parentLinkConfig == null ? null : parentLinkConfig.getParentLinkConfig();
    PluginConfig pluginConfig = parentLinkConfig == null || parentLinkConfig.getPluginDefinition() == null ?
            null : parentLinkConfig.getPluginDefinition().getPluginConfig();
    DomainObjectSurferConfig domainObjectSurferConfig = (pluginConfig instanceof DomainObjectSurferConfig) ?
            (DomainObjectSurferConfig) pluginConfig : null;
    return domainObjectSurferConfig == null ? null : domainObjectSurferConfig.getFormViewerConfig();
  }

  /**
   * Устанавливаем название для элемента "хлебные крошки" в зависимости от конфигурации
   * <child-collection-viewer <child-collection-viewer breadCrumbColumn="name" breadCrumbMaxChars=N>
   *
   * @param childCollectionViewerConfig конфигурация дочерней коллекции
   * @param row                         строка родительской коллекции из колонки которой берем название
   * @return новое значение breadCrumb
   */
  public static String prepareBreadCrumb(ChildCollectionViewerConfig childCollectionViewerConfig,
                                         HashMap<String, Value> row) {
    String breadCrumbValue = childCollectionViewerConfig.getBreadCrumb();
    if (childCollectionViewerConfig.getBreadCrumbColumn() != null) {
      if(row.get(childCollectionViewerConfig.getBreadCrumbColumn())!=null){
        breadCrumbValue = row.get(childCollectionViewerConfig.getBreadCrumbColumn()).toString();
        if(childCollectionViewerConfig.getBreadCrumbMaxChars()!=null
            && breadCrumbValue.length()>childCollectionViewerConfig.getBreadCrumbMaxChars()){
          breadCrumbValue = breadCrumbValue.substring(0,childCollectionViewerConfig.getBreadCrumbMaxChars()-3)
              +BC_SHORT_SUFFIX;
        }
      }
    }
    return breadCrumbValue;
  }
}
