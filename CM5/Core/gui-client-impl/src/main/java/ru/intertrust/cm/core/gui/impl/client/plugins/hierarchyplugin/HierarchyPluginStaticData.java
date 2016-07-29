package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 28.07.2016
 * Time: 11:10
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyPluginStaticData {

    public static final String STYLE_WRAP_WIDGET = "wrapWidget";
    public static final String STYLE_WRAP_PANEL = "wrapPanel";
    public static final String STYLE_PARENT_PANEL = "parentPanel";
    public static final String STYLE_HEADER_PANEL = "headerPanel";
    public static final String STYLE_CHILD_PANEL = "childPanel";
    public static final String STYLE_GROUP_NAME = "groupName";
    public static final String STYLE_REPRESENTATION_CELL = "representationCell";
    public static final String STYLE_WRAP_CELL = "wrapCell";
    public static final String STYLE_HEADER_CELL = "headerCell";
    public static final String STYLE_CHILD_CELL = "childCell";
    public static final String STYLE_FIELD_NAME = "fieldName";
    public static final String STYLE_FIELD_VALUE = "fieldValue";

    public enum Actions {
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
}
