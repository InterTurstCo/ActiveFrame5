package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 11:55
 */
public class Command implements Dto {
    private String name;
    private String componentName;
    private Dto parameter;

    public Command() {
    }

    public Command(String name, String componentName, Dto parameter) {
        this.name = name;
        this.componentName = componentName;
        this.parameter = parameter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Dto getParameter() {
        return parameter;
    }

    public void setParameter(Dto parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "Command {" +
                "componentName='" + componentName + '\'' +
                ", parameter=" + parameter +
                '}';
    }
}
