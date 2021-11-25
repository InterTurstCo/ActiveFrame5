package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.dao.access.DynamicGroupSettings;

public class DynamicGroupSettingsImpl implements DynamicGroupSettings{
    /**
     * Флаг выключения расчета динамических групп
     */
    private boolean disableGroupCalculation;
    
    /**
     * Флаг выключения расчета плоской структуры состава группы
     */
    private boolean disableGroupUncover;
    
    @Override
    public boolean isDisableGroupUncover() {
        return disableGroupUncover;
    }

    @Override
    public void setDisableGroupUncover(boolean value) {
        disableGroupUncover = value;
    }

    @Override
    public boolean isDisableGroupCalculation() {        
        return disableGroupCalculation;
    }

    @Override
    public void setDisableGroupCalculation(boolean value) {
        disableGroupCalculation = value;        
    }

}
