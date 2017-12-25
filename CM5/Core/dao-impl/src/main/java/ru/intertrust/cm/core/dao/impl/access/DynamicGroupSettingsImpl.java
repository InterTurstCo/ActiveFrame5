package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.dao.access.DynamicGroupSettings;

public class DynamicGroupSettingsImpl implements DynamicGroupSettings{
    /**
     * Флаг выключения расчета динамических групп
     */
    @org.springframework.beans.factory.annotation.Value("${disable.group.calculation:false}")
    private boolean disableGroupCalculation;
    
    /**
     * Флаг выключения расчета плоской структуры состава группы
     */
    @org.springframework.beans.factory.annotation.Value("${disable.group.uncover:false}")
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
