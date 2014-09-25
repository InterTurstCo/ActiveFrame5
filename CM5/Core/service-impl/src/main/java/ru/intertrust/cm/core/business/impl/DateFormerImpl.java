package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.DateFormer;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.PersonProfile;
import ru.intertrust.cm.core.business.api.dto.Profile;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;

/**
 * Имплементация сервиса формирования дат
 * @author larin
 *
 */
public class DateFormerImpl implements DateFormer {
    @Autowired
    private ProfileService profileService;

    @Override
    public String format(TimelessDate date, Id personId) {
        String result = "";
        if (date != null) {
            Locale locale = getPersonLocale(personId);
            DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, date.getYear());
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
            result = formatter.format(calendar.getTime());
        }
        return result;
    }

    @Override
    public String format(Date date, Id personId) {
        String result = "";
        if (date != null) {
            Locale locale = getPersonLocale(personId);
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
            result = formatter.format(date);
        }
        return result;
    }

    @Override
    public String format(DateTimeWithTimeZone date, Id personId) {
        String result = "";
        if (date != null) {
            Locale locale = getPersonLocale(personId);
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(date.getTimeZoneContext().getTimeZoneId()));

            calendar.set(Calendar.YEAR, date.getYear());
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
            calendar.set(Calendar.MINUTE, date.getMinutes());
            calendar.set(Calendar.SECOND, date.getSeconds());
            calendar.set(Calendar.MILLISECOND, date.getMilliseconds());
            result = formatter.format(calendar.getTime());
        }
        return result;
    }

    private Locale getPersonLocale(Id personId) {
        PersonProfile profile = profileService.getPersonProfileByPersonId(personId);
        String locale = profile.getString(ProfileService.LOCALE);
        return Locale.forLanguageTag(locale);
    }

}
