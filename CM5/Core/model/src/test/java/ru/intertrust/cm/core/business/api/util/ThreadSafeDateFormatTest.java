package ru.intertrust.cm.core.business.api.util;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

public class ThreadSafeDateFormatTest {

    private static final int THREADS_COUNT = 100;
    
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
    private static final Locale LOCALE = Locale.US;

    @Test
    public void testFormat() {
        final Date date = new Date();
        String expectedDate = new SimpleDateFormat(DATE_PATTERN).format(date);
        String actualDate = ThreadSafeDateFormat.format(date, DATE_PATTERN);
        assertEquals(expectedDate, actualDate);

        expectedDate = new SimpleDateFormat(DATE_PATTERN, LOCALE).format(date);
        actualDate = ThreadSafeDateFormat.format(date, DATE_PATTERN, LOCALE);
        
        assertEquals(expectedDate, actualDate);

        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, LOCALE);
        TimeZone timeZone = TimeZone.getTimeZone(ModelUtil.DEFAULT_TIME_ZONE_ID);
        dateFormat.setTimeZone(timeZone);
        expectedDate = dateFormat.format(date);
        
        actualDate = ThreadSafeDateFormat.format(date, DATE_PATTERN, LOCALE, timeZone);
        assertEquals(expectedDate, actualDate);
      
    }
    
    /**
     * Одновременный вызов ThreadSafeDateFormat.format() из нескольких потоков.
     */
    @Test
    public void testConcurrentFormat() {
        final Date date = new Date();
        final String expectedDate = new SimpleDateFormat(DATE_PATTERN).format(date);

        final CountDownLatch startSignal = new CountDownLatch(1);
        
        for (int i = 0; i < THREADS_COUNT; i++) {
            new Thread() {
                public void run() {
                    try {
                        startSignal.await();
                        String actualDate = ThreadSafeDateFormat.format(date, DATE_PATTERN);
                        assertEquals(expectedDate, actualDate);

                    } catch (InterruptedException ex) {

                    }
                    ;
                }
            }.start();
        }
        startSignal.countDown();
    }

    @Test
    public void testParse() {
        final Date expectedDate = new Date();
        String testStringDate = new SimpleDateFormat(DATE_PATTERN).format(expectedDate);
        System.out.println(" TestStringDate: " + testStringDate);
        Date actualDate = ThreadSafeDateFormat.parse(testStringDate, DATE_PATTERN);        
        assertEquals(actualDate, expectedDate);

        Date expectedDate2 = ThreadSafeDateFormat.parse(testStringDate, DATE_PATTERN);        
        assertEquals(expectedDate2, expectedDate);       
        
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        TimeZone timeZone = TimeZone.getTimeZone(ModelUtil.DEFAULT_TIME_ZONE_ID);
        dateFormat.setTimeZone(timeZone);
        testStringDate = dateFormat.format(expectedDate);
        actualDate = ThreadSafeDateFormat.parse(testStringDate, DATE_PATTERN, timeZone);   
        assertEquals(expectedDate, actualDate);
        
        dateFormat = new SimpleDateFormat(DATE_PATTERN, LOCALE);
        timeZone = TimeZone.getTimeZone(ModelUtil.DEFAULT_TIME_ZONE_ID);
        dateFormat.setTimeZone(timeZone);
        testStringDate = dateFormat.format(expectedDate);
        
        actualDate = ThreadSafeDateFormat.parse(testStringDate, DATE_PATTERN, LOCALE, timeZone);   
        assertEquals(expectedDate, actualDate);

        
    }

}
