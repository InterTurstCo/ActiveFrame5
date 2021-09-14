package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.DateFormer;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DateFormerTest {
    private final static boolean IS_JAVA_8 = System.getProperty("java.version").startsWith("1.8");
    // Так себе решение... но тестируемый класс особо никому не нужен,
    // заметил его использование в 1 месте, в import-notification_text.csv
    private final static boolean IS_JAVA_11 = System.getProperty("java.version").startsWith("11");

    @InjectMocks
    private DateFormer former = new DateFormerImpl();
    
    @Mock
    private ProfileService profileService;
    
    @Before
    public void setUp() {
        Profile profile = new ProfileObject();
        profile.setString(ProfileService.LOCALE, "RU");        
        when(profileService.getPersonProfileByPersonId((Id)anyObject())).thenReturn(profile);
    }
    
    @Test
    public void testTimestamp() throws ParseException{
        SimpleDateFormat detaeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        Date testDate = detaeFormat.parse("2014-02-28 18:36:41 356");
        String result = former.format(testDate, new RdbmsId(1,1));
        if (IS_JAVA_8) {
            assertTrue(result.equals("28 февраля 2014 г. 18:36"));
        } else if (IS_JAVA_11) {
            assertTrue(result.equals("28 февраля 2014 г., 18:36"));
        } else {
            assertTrue(result.equals("28 Февраль 2014 г. 18:36"));
        }
    }

    @Test
    public void testTimelessDate() throws ParseException{
        TimelessDate testDate = new TimelessDate(2014, 1, 28);
        String result = former.format(testDate, new RdbmsId(1,1));
        if (IS_JAVA_8 || IS_JAVA_11) {
            assertTrue(result.equals("28 февраля 2014 г."));
        } else {
            assertTrue(result.equals("28 Февраль 2014 г."));
        }
    }

    @Test
    public void testDateTimeWithTimeZone() throws ParseException{
        DateTimeWithTimeZone testDate = new DateTimeWithTimeZone("GMT+5", 2014, 1, 28, 18, 36, 41, 356);
        
        String result = former.format(testDate, new RdbmsId(1,1));
        if (IS_JAVA_8) {
            assertTrue(result.equals("28 февраля 2014 г. 18:36:41 GMT+05:00"));
        } else if (IS_JAVA_11) {
            assertTrue(result.equals("28 февраля 2014 г., 18:36:41 GMT+05:00"));
        } else {
            assertTrue(result.equals("28 Февраль 2014 г. 18:36:41 GMT+05:00"));
        }
    }

}
