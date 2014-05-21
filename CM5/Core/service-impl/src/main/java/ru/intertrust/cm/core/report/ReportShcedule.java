package ru.intertrust.cm.core.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.NotificationService;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.QueryParameter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.RelativeDate;
import ru.intertrust.cm.core.business.api.dto.RelativeDateBase;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.business.api.dto.ReportShceduleParameter;
import ru.intertrust.cm.core.business.api.dto.ShceduleTaskReportParam;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationContext;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationPriority;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.api.schedule.SheduleType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.FatalException;

import com.ibm.icu.util.Calendar;

@ScheduleTask(name = "TestScheduleMultiple", minute = "0", hour = "1", type = SheduleType.Multipliable)
public class ReportShcedule implements ScheduleTaskHandle {
    private static final String PERIODIC_REPORT = "PERIODIC_REPORT";
    private static final int DEFAULT_KEEP_DAYS = 1;

    @EJB
    private ReportService reportService;

    @EJB
    private NotificationService notificationService;

    @Autowired
    private CollectionsDao colecctionService;

    @Autowired
    private DomainObjectDao domainObjectService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public String execute(SessionContext sessionContext,
            ScheduleTaskParameters parameters) throws InterruptedException {
        long start = System.currentTimeMillis();

        ReportShceduleParameter reportParameter = (ReportShceduleParameter) parameters;

        //Получение контекста периодического отчета
        if (reportParameter.getReportContextQuery() != null && reportParameter.getReportContextQuery().length() > 0) {
            IdentifiableObjectCollection collection = colecctionService.findCollectionByQuery(reportParameter.getReportContextQuery(), 0, 0,
                    accessControlService.createSystemAccessToken(this.getClass().getName()));
            //Отчет формируется для каждого элемента контекста
            for (IdentifiableObject context : collection) {
                generateReport(reportParameter, context.getId());
            }
        } else {
            //Формирование отчета без контекста
            generateReport(reportParameter, null);
        }
        return "Report " + reportParameter.getName() + " generated at " + (System.currentTimeMillis() - start) + " ms";
    }

    private void generateReport(ReportShceduleParameter reportParameter, Id contextId) {
        ReportResult reportResult = reportService.generate(
                reportParameter.getName(), getParameters(reportParameter, contextId), DEFAULT_KEEP_DAYS);

        NotificationContext notificationContext = new NotificationContext();
        notificationContext.addContextObject("report", reportResult.getResultId());
        notificationContext.addContextObject("context", contextId);
        notificationService.sendOnTransactionSuccess(PERIODIC_REPORT, null, getAddressee(reportParameter, contextId), NotificationPriority.NORMAL,
                notificationContext);
    }

    /**
     * Получение адресатов
     * @param reportParameter
     * @param contextId
     * @return
     */
    private List<NotificationAddressee> getAddressee(ReportShceduleParameter reportParameter, Id contextId) {
        List<NotificationAddressee> result = new ArrayList<NotificationAddressee>();
        List<Value> parameters = new ArrayList<Value>();
        if (contextId != null && reportParameter.getAddresseeQuery().indexOf("{0}") > 0) {
            parameters.add(new ReferenceValue(contextId));
        }
        IdentifiableObjectCollection collection = colecctionService.findCollectionByQuery(reportParameter.getAddresseeQuery(), parameters, 0, 0,
                accessControlService.createSystemAccessToken(this.getClass().getName()));
        for (IdentifiableObject identifiableObject : collection) {
            DomainObject addresseeObject =
                    domainObjectService.find(identifiableObject.getId(), accessControlService.createSystemAccessToken(this.getClass().getName()));
            NotificationAddressee addressee = null;
            if (isTypeOf(addresseeObject, "person")) {
                addressee = new NotificationAddresseePerson(addresseeObject.getId());
            } else if (isTypeOf(addresseeObject, "user_group")) {
                addressee = new NotificationAddresseeGroup(addresseeObject.getId());
            } else {
                throw new FatalException("AddresseeQuery=[" + reportParameter.getAddresseeQuery() + "] return not valid type. Need return person or group ids");
            }
            result.add(addressee);
        }
        return result;
    }

    private boolean isTypeOf(DomainObject objectId, String typeName) {
        boolean result = false;
        if (objectId.getTypeName().equalsIgnoreCase(typeName)) {
            result = true;
        } else {
            Collection<DomainObjectTypeConfig> childTypes = configurationExplorer.findChildDomainObjectTypes(typeName, true);
            for (DomainObjectTypeConfig childDomainObjectTypeConfig : childTypes) {
                if (childDomainObjectTypeConfig.getName().equalsIgnoreCase(objectId.getTypeName())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Получение параметров
     * @param reportParameter
     * @param contextId
     * @return
     */
    protected Map<String, Object> getParameters(ReportShceduleParameter reportParameter, Id contextId) {
        Map<String, Object> result = new HashMap<String, Object>();

        for (ShceduleTaskReportParam parameter : reportParameter
                .getParameters()) {
            if (parameter.getValue() instanceof String || parameter.getValue() instanceof Long || parameter.getValue() instanceof Date) {
                result.put(parameter.getName(), parameter.getValue());
            } else if (parameter.getValue() instanceof RelativeDate) {
                RelativeDate relativeDateParam = (RelativeDate) parameter
                        .getValue();
                // Расчитываем базовую дату
                Calendar calendar = Calendar.getInstance(new Locale("ru"));
                calendar.setTime(new Date());
                calendar.set(Calendar.MILLISECOND, 0);
                if (relativeDateParam.getBaseDate() != null) {
                    if (relativeDateParam.getBaseDate().equals(RelativeDateBase.START_DAY)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.END_DAY)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.START_WEEK)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.END_WEEK)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                        calendar.add(Calendar.DAY_OF_MONTH, 6);
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.START_MONTH)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.END_MONTH)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.START_YEAR)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.MONTH, 0);
                    } else if (relativeDateParam.getBaseDate().equals(RelativeDateBase.END_YEAR)) {
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.set(Calendar.MONTH, 11);
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    }
                }

                // Расчитываем смещение
                if (relativeDateParam.getOffsetMin() != null) {
                    calendar.add(Calendar.MINUTE, relativeDateParam.getOffsetMin());
                }
                if (relativeDateParam.getOffsetHour() != null) {
                    calendar.add(Calendar.HOUR_OF_DAY, relativeDateParam.getOffsetHour());
                }
                if (relativeDateParam.getOffsetDay() != null) {
                    calendar.add(Calendar.DAY_OF_MONTH, relativeDateParam.getOffsetDay());
                }
                if (relativeDateParam.getOffsetMonth() != null) {
                    calendar.add(Calendar.MONTH, relativeDateParam.getOffsetMonth());
                }
                if (relativeDateParam.getOffsetYear() != null) {
                    calendar.add(Calendar.YEAR, relativeDateParam.getOffsetYear());
                }

                result.put(parameter.getName(), calendar.getTime());
            } else if (parameter.getValue() instanceof QueryParameter) {
                QueryParameter queryParameter = (QueryParameter) parameter.getValue();
                List<Value> parameters = new ArrayList<Value>();
                if (contextId != null && queryParameter.getQuery().indexOf("{0}") > 0) {
                    parameters.add(new ReferenceValue(contextId));
                }
                IdentifiableObjectCollection collection = colecctionService.findCollectionByQuery(queryParameter.getQuery(), parameters, 0, 0,
                        accessControlService.createSystemAccessToken(this.getClass().getName()));
                result.put(parameter.getName(), collection.get(0).getId());
            }
        }

        return result;
    }

}
