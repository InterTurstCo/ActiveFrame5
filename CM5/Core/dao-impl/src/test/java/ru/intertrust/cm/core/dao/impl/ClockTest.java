package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ru.intertrust.cm.core.dao.api.Stamp;

public class ClockTest {
    
    @Test
    public void testStampCompare() {
        Stamp stamp0 = new StampImpl(100, 100);
        
        assertTrue(stamp0.compareTo(null) > 0);        
        assertTrue(stamp0.compareTo(new StampImpl(0, 0)) > 0);
        assertTrue(stamp0.compareTo(new StampImpl(0, 100)) > 0);
        assertTrue(stamp0.compareTo(new StampImpl(0, 200)) > 0);
        assertTrue(stamp0.compareTo(new StampImpl(100, 0)) > 0);
        assertTrue(stamp0.compareTo(new StampImpl(100, 100)) == 0);
        assertTrue(stamp0.compareTo(new StampImpl(100, 200)) < 0);
        assertTrue(stamp0.compareTo(new StampImpl(200, 0)) < 0);
        assertTrue(stamp0.compareTo(new StampImpl(200, 100)) < 0);
        assertTrue(stamp0.compareTo(new StampImpl(200, 200)) < 0);
    }

    @Test
    public void testClockNext() throws InterruptedException {
        ClockImpl clock = new ClockImpl();
        
        // Проверяем мотонное возрастание в пределах 1 мс
        Stamp parentStamp = clock.nextStamp();        
        for (int i=0; i<1000; i++) {
            Stamp stamp = clock.nextStamp();
            assertTrue(stamp.compareTo(parentStamp)>0);
            parentStamp = stamp;
        }
        
        // Пауза 1 мс
        Thread.currentThread().sleep(1);
        
        // Проверяем мотонное возрастание в пределах следующей мс
        for (int i=0; i<1000; i++) {
            Stamp stamp = clock.nextStamp();
            assertTrue(stamp.compareTo(parentStamp)>0);
            parentStamp = stamp;
        }
    }

}
