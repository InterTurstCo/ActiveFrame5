package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.DateFormer;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.PersonProfile;
import ru.intertrust.cm.core.business.api.dto.PersonProfileObject;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

@RunWith(MockitoJUnitRunner.class)
public class DateFormerTest {
    @InjectMocks
    private DateFormer former = new DateFormerImpl();
    
    @Mock
    private ProfileService profileService;
    
    @Before
    public void setUp() {
        PersonProfile profile = new PersonProfileObject();
        profile.setString(ProfileService.LOCALE, "RU");        
        when(profileService.getPersonProfileByPersonId((Id)anyObject())).thenReturn(profile);
    }
    
    @Test
    public void testTimestamp() throws ParseException{
        SimpleDateFormat detaeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        Date testDate = detaeFormat.parse("2014-02-28 18:36:41 356");
        String result = former.format(testDate, new RdbmsId(1,1));
        assertTrue(result.equals("28 февраля 2014 г., 18:36"));
    }

    @Test
    public void testTimelessDate() throws ParseException{
        TimelessDate testDate = new TimelessDate();
        testDate.setYear(2014);
        testDate.setMonth(1);
        testDate.setDayOfMonth(28);
        String result = former.format(testDate, new RdbmsId(1,1));
        assertTrue(result.equals("28 февраля 2014 г."));
    }

    @Test
    public void testDateTimeWithTimeZone() throws ParseException{
        DateTimeWithTimeZone testDate = new DateTimeWithTimeZone(4, 2014, 1, 28, 18, 36, 41, 356);
        
        String result = former.format(testDate, new RdbmsId(1,1));
        assertTrue(result.equals("28 февраля 2014 г., 22:36"));
    }
    
}
