package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravil on 22.01.2018.
 */
@ComponentName("BalancerControl.plugin")
public class BalancerControlPluginHandler extends PluginHandler {
    public PluginData initialize(Dto config) {
        BalancerControlPluginData pData = new BalancerControlPluginData();

        BalancerControlPluginStatRow testRow = new BalancerControlPluginStatRow();
        testRow.setDataSource("MASTER");
        testRow.setDelay(12);
        testRow.setState(ServerState.NORMAL);
        testRow.setDelayDbms(0);
        testRow.settXId("100908734");
        testRow.setPercentageHitNow(100);
        testRow.setFaultsNow(0);
        testRow.setSelectSecNow(5);
        testRow.setPercentageHitHour(800);
        testRow.setFaultsHour(10);
        testRow.setSelectSecHour(23);
        pData.getRows().add(testRow);

        BalancerControlPluginTypesRow typesRow = new BalancerControlPluginTypesRow();
        typesRow.setType("RKK");
        typesRow.setMasterTime("21.02.2018 17:55:34");
        typesRow.setImaginary("+");
        typesRow.setSlave1dt("20.96");
        typesRow.setSlave2dt("0");
        typesRow.setSlave3dt("80");
        typesRow.setSlave4dt("940.34");
        typesRow.setSlave5dt("100");
        pData.getTypes().add(typesRow);

        return pData;
    }

    public Dto refreshPage(Dto request){
        return new BalancerControlPluginData();
    }

    public Dto turnOnOff(Dto request){
        return new BalancerControlPluginData();
    }

    public Dto checkServer(Dto request){
        BalancerControlPluginData dto = new BalancerControlPluginData();
        dto.setMessage("Сервер доступен");
        return dto;
    }

    public Dto extStatOnOff(Dto request){
        BalancerControlPluginData dto = new BalancerControlPluginData();
        dto.setMessage("Расширенная статистика включена");
        return dto;
    }

    public Dto extStatReset(Dto request){
        BalancerControlPluginData dto = new BalancerControlPluginData();
        return dto;
    }

    public Dto saveConfig(Dto request){
        BalancerControlPluginData dto = new BalancerControlPluginData();
        dto.setMessage("Настройки сохранены");
        return dto;
    }
}
