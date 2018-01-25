package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravil on 22.01.2018.
 */
public class BalancerControlPluginData extends PluginData {
    private List<BalancerControlPluginStatRow> rows;
    private List<BalancerControlPluginTypesRow> types;
    private Boolean turnOn = false;
    private List<String> serversToOperate;
    private String message;
    private BalancerControlPluginConfiguration configuration;

    public BalancerControlPluginData() {

    }

    public List<BalancerControlPluginStatRow> getRows() {
        if (rows == null) {
            rows = new ArrayList<>();
        }
        return rows;
    }

    public void setRows(List<BalancerControlPluginStatRow> rows) {
        this.rows = rows;
    }

    public List<BalancerControlPluginTypesRow> getTypes() {
        if (types == null) {
            types = new ArrayList<>();
        }
        return types;

    }

    public void setTypes(List<BalancerControlPluginTypesRow> types) {
        this.types = types;
    }

    public Boolean getTurnOn() {
        return turnOn;
    }

    public void setTurnOn(Boolean turnOn) {
        this.turnOn = turnOn;
    }

    public List<String> getServersToOperate() {
        if(serversToOperate==null)
            serversToOperate = new ArrayList<>();
        return serversToOperate;
    }

    public void setServersToOperate(List<String> serversToOperate) {
        this.serversToOperate = serversToOperate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BalancerControlPluginConfiguration getConfiguration() {
        if(configuration==null)
            configuration = new BalancerControlPluginConfiguration();
        return configuration;
    }

    public void setConfiguration(BalancerControlPluginConfiguration configuration) {
        this.configuration = configuration;
    }
}
