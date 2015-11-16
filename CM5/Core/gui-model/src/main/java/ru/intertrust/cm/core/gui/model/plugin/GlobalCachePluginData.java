package ru.intertrust.cm.core.gui.model.plugin;


/**
 * Класс содержит модель данных для плагина статистики
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 27.10.2015
 */
public class GlobalCachePluginData extends PluginData {

    private GlobalCacheStatPanel statPanel;

    private String errorMsg;


    public GlobalCacheStatPanel getStatPanel() {
        return statPanel;
    }

    public void setStatPanel(GlobalCacheStatPanel statPanel) {
        this.statPanel = statPanel;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public GlobalCachePluginData(){}
}
