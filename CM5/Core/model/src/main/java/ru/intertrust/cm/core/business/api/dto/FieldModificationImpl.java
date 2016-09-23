package ru.intertrust.cm.core.business.api.dto;

public class FieldModificationImpl implements FieldModification {

    private String name;
    private Value baseValue;
    private Value comparedValue;

    public FieldModificationImpl(String name, Value baseValue, Value comparedValue) {
        this.name = name;
        this.baseValue = baseValue;
        this.comparedValue = comparedValue;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Value getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(Value baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public Value getComparedValue() {
        return comparedValue;
    }

    public void setComparedValue(Value comparedValue) {
        this.comparedValue = comparedValue;
    }

    @Override
    public String toString() {
        return new StringBuilder(name)
                .append(": ")
                .append(baseValue == null ? "null" : baseValue.get())
                .append(" -> ")
                .append(comparedValue == null ? "null" : comparedValue.get())
                .toString();
    }

}
