package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 28.07.2016
 * Time: 11:10
 * To change this template use File | Settings | File and Code Templates.
 */
public interface HierarchyPluginConstants {

    String PLUGIN_COMPONENT_NAME = "hierarchy.plugin";
    String GET_COL_ROWS_METHOD_NAME = "getCollectionItems";
    String SAVE_PLUGIN_HISTORY = "savePluginHistory";
    String RESTORE_PLUGIN_HISTORY = "restorePluginHistory";
    String STYLE_WRAP_WIDGET = "wrapWidget";
    String STYLE_WRAP_PANEL = "wrapPanel";
    String STYLE_PARENT_PANEL = "parentPanel";
    String STYLE_HEADER_PANEL = "headerPanel";
    String STYLE_CHILD_PANEL = "childPanel";
    String STYLE_GROUP_NAME = "groupName";
    String STYLE_REPRESENTATION_CELL = "representationCell";
    String STYLE_WRAP_CELL = "wrapCell";
    String STYLE_FOCUS_WRAP_CELL = "focusWrapCell";
    String STYLE_HEADER_CELL = "headerCell";
    String STYLE_CHILD_CELL = "childCell";
    String STYLE_FIELD_NAME = "fieldName";
    String STYLE_FIELD_VALUE = "fieldValue";
    String REF_TYPE_NAME = "reference";

    enum Actions {
        GROUPREFRESH("Обновить"),
        GROUPSORT("Сортировать"),
        GROUPADD("Добавить");

        private String tooltip;

        Actions(String aTooltip){
            tooltip = aTooltip;
        }

        public String toString(){
            return tooltip;
        }
    }

    enum NodeType {
        GROUP, ROW
    }
}
