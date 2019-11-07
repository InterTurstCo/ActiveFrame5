package ru.intertrust.cm.core.business.report;

import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RelativeDate;
import ru.intertrust.cm.core.business.api.dto.RelativeDateBase;
import ru.intertrust.cm.core.business.api.dto.ReportShceduleParameter;
import ru.intertrust.cm.core.business.api.dto.ShceduleTaskReportParam;
import ru.intertrust.cm.core.report.ReportShcedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TestReportService {

    private class ReportShceduleInt extends ReportShcedule {
        public Map<String, Object> getParametersPub(ReportShceduleParameter reportParameter, Id contextId) {
            return super.getParameters(reportParameter, contextId);
        }
    }

    @Test
    public void testReportShceduleGetParameters() throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance(new Locale("ru"));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int weekyear = calendar.get(Calendar.WEEK_OF_YEAR);
        
        RelativeDate relDateParam = new RelativeDate();
        relDateParam.setBaseDate(RelativeDateBase.START_DAY);
        checkParam(relDateParam, dateFormatter.parse(day + "-" + (month + 1) + "-" + year + " 00:00:00"));

        relDateParam.setBaseDate(RelativeDateBase.END_DAY);
        checkParam(relDateParam, dateFormatter.parse(day + "-" + (month + 1) + "-" + year + " 23:59:59"));

        Calendar weekCalendar = (Calendar)calendar.clone();
        weekCalendar.setWeekDate(year, weekyear, calendar.getFirstDayOfWeek());        
        relDateParam.setBaseDate(RelativeDateBase.START_WEEK);
        checkParam(relDateParam, dateFormatter.parse(weekCalendar.get(Calendar.DAY_OF_MONTH) + "-" + (weekCalendar.get(Calendar.MONTH) + 1) + "-" + weekCalendar.get(Calendar.YEAR) + " 00:00:00"));
        
        weekCalendar.add(Calendar.DAY_OF_MONTH, 6);    
        relDateParam.setBaseDate(RelativeDateBase.END_WEEK);
        checkParam(relDateParam, dateFormatter.parse(weekCalendar.get(Calendar.DAY_OF_MONTH) + "-" + (weekCalendar.get(Calendar.MONTH) + 1) + "-" + weekCalendar.get(Calendar.YEAR) + " 23:59:59"));
                
        relDateParam.setBaseDate(RelativeDateBase.START_MONTH);
        checkParam(relDateParam, dateFormatter.parse("01-" + (month + 1) + "-" + year + " 00:00:00"));

        relDateParam.setBaseDate(RelativeDateBase.END_MONTH);
        checkParam(relDateParam, dateFormatter.parse(calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "-" + (month + 1) + "-" + year + " 23:59:59"));
    
        relDateParam.setBaseDate(RelativeDateBase.START_YEAR);
        checkParam(relDateParam, dateFormatter.parse("01-01-" + year + " 00:00:00"));
        
        relDateParam.setBaseDate(RelativeDateBase.END_YEAR);
        checkParam(relDateParam, dateFormatter.parse("31-12-" + year + " 23:59:59"));
        
        relDateParam.setBaseDate(RelativeDateBase.START_DAY);
        relDateParam.setOffsetMin(5);
        checkParam(relDateParam, dateFormatter.parse(day + "-" + (month + 1) + "-" + year + " 00:05:00"));

        relDateParam.setOffsetHour(1);
        checkParam(relDateParam, dateFormatter.parse(day + "-" + (month + 1) + "-" + year + " 01:05:00"));

        relDateParam.setOffsetDay(1);
        checkParam(relDateParam, dateFormatter.parse((day + 1) + "-" + (month + 1) + "-" + year + " 01:05:00"));

        relDateParam.setOffsetMonth(1);
        checkParam(relDateParam, dateFormatter.parse((day + 1) + "-" + (month + 2) + "-" + year + " 01:05:00"));

        relDateParam.setOffsetYear(1);
        checkParam(relDateParam, dateFormatter.parse((day + 1) + "-" + (month + 2) + "-" + (year + 1) + " 01:05:00"));
    }
    
    private void checkParam(RelativeDate relDateParam, Date etalonDate){
        ReportShceduleInt reportShcedule = new ReportShceduleInt();
        ReportShceduleParameter reportShceduleParameter = new ReportShceduleParameter();
        reportShceduleParameter.setParameters(new ArrayList<ShceduleTaskReportParam>());
        reportShceduleParameter.getParameters().add(new ShceduleTaskReportParam("REL_DATE", relDateParam));
        Map<String, Object> result = reportShcedule.getParametersPub(reportShceduleParameter, null);
        
       // Assert.assertTrue(relDateParam.toString(), ((Date)result.get("REL_DATE")).compareTo(etalonDate) == 0);
    }
}
