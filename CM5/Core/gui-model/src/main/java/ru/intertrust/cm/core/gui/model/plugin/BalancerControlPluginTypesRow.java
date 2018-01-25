package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by Ravil on 25.01.2018.
 */
public class BalancerControlPluginTypesRow implements Dto {
    private String type;
    private String masterTime;
    private String imaginary;
    private String slave1dt;
    private String slave2dt;
    private String slave3dt;
    private String slave4dt;
    private String slave5dt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMasterTime() {
        return masterTime;
    }

    public void setMasterTime(String masterTime) {
        this.masterTime = masterTime;
    }

    public String getImaginary() {
        return imaginary;
    }

    public void setImaginary(String imaginary) {
        this.imaginary = imaginary;
    }

    public String getSlave1dt() {
        return slave1dt;
    }

    public void setSlave1dt(String slave1dt) {
        this.slave1dt = slave1dt;
    }

    public String getSlave2dt() {
        return slave2dt;
    }

    public void setSlave2dt(String slave2dt) {
        this.slave2dt = slave2dt;
    }

    public String getSlave3dt() {
        return slave3dt;
    }

    public void setSlave3dt(String slave3dt) {
        this.slave3dt = slave3dt;
    }

    public String getSlave4dt() {
        return slave4dt;
    }

    public void setSlave4dt(String slave4dt) {
        this.slave4dt = slave4dt;
    }

    public String getSlave5dt() {
        return slave5dt;
    }

    public void setSlave5dt(String slave5dt) {
        this.slave5dt = slave5dt;
    }
}
