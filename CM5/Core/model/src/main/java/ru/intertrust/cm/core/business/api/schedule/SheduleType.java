package ru.intertrust.cm.core.business.api.schedule;

public enum SheduleType {
    Singleton(0),
    Multipliable(1);

    long value;
    
    private SheduleType(long value){
        this.value = value;
    }
    
    public long toLong(){
        return this.value;
    }
    
    public SheduleType valueOf(long value){
        if (value == 0){
            return SheduleType.Singleton;
        }else{
            return SheduleType.Multipliable;
        }
    }
}
