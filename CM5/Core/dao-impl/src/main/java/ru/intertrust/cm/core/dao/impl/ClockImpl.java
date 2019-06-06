package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.core.dao.api.Clock;

public class ClockImpl implements Clock{
    private StampImpl lastStamp;
    
    @Override
    public Stamp<?> nextStamp() {
        StampImpl result = null;
        long curentTime = System.currentTimeMillis();
        
        // В принципе, для нужд глобального кэша синхронизация не обязательна, так как выше уже будет синхронизация. 
        // Добавляем синхронизацию здесь если понадобится использовать данный сервис какими то другими сервисами, отличными от глобального кэша.
        synchronized (ClockImpl.class) {
            if (lastStamp != null && curentTime == lastStamp.getHiDigit()) {
                result = new StampImpl(curentTime, lastStamp.getLowDigit() + 1);
            }else {
                result = new StampImpl(curentTime, 0);
            }
            lastStamp = result;
        }
        return result;
    }

}
