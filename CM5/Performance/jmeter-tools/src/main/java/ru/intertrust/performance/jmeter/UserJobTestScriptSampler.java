package ru.intertrust.performance.jmeter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;

/**
 * Класс, реализующий выполнение тестового сценария работы пользователя.
 * 1) открыть одно из представлений со списком документов;
 * 2) пролистать список на несколько страниц (примерно 100 записей);
 * 3) пересортировать список по атрибуту "дата регистрации", открыть 1-ю страницу (самые новые документы);
 * 4) открыть случайный документ из списка;
 * 5) открыть "дерево" поручений по данному документу;
 * 6) открыть список сотрудников из орг.структуры, сортированный по ФИО, начиная со случайной буквы;
 * 7) пролистать список сотрудников несколько раз;
 * 8) создать/сохранить поручение по документу для случайно выбранного сотрудника;
 *
 * @author Dmitry Lyakin Date: 12.09.13 Time: 09:23
 *
 */
public class UserJobTestScriptSampler extends AbstractSampler {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(UserJobTestScriptSampler.class);

    public static final String PROVIDER_URL = "JndiSampler.provider_url";
    public static final String SECURITY_PRINCIPAL = "JndiSampler.security_principal";
    public static final String SECURITY_CREDENTIALS = "JndiSampler.security_credentials";

    public static final String CONNECTION_CONFIG = "JndiSampler.connection_config";
    //Количество записей в коллекции
    public static final int PAGE_COUNT=10;
    @Override
    public SampleResult sample(Entry e) {
        int randomInt = 0;
        IdentifiableObject element = null;
        SampleResult res = new SampleResult();
        String providerUrl = getProviderUrl();
        String securityPrincipal = getSecurityPrincipal();
        String securityCreditals = getSecurityCredentials();
        //Установка настроек Jndi
        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        // Адрес подключения
        jndiProps.put(Context.PROVIDER_URL, providerUrl);
        // Имя пользователя в бд
        jndiProps.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        // Пароль
        jndiProps.put(Context.SECURITY_CREDENTIALS, securityCreditals);
        jndiProps.put("jboss.naming.client.ejb.context", true);
        jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        try {
            Random randomGenerator = SecureRandom.getInstanceStrong();

            res.sampleStart();
            // Инициализация контекста
            Context ctx = new InitialContext(jndiProps);
            // CollectionService
            CollectionsService collectionService =
                    (CollectionsService) ctx
                            .lookup("java:cm-sochi-1.0-SNAPSHOT/web-app/CollectionsServiceImpl!ru.intertrust.cm.core.business.api.CollectionsService$Remote");
            // CrudService
            CrudService crudService =
                    (CrudService) ctx.lookup("java:cm-sochi-1.0-SNAPSHOT/web-app/CrudServiceImpl!ru.intertrust.cm.core.business.api.CrudService$Remote");
            // Задание сортировка коллекции по идентификатору(id)
            SortCriterion scId = new SortCriterion("id", Order.ASCENDING);
            SortOrder soId = new SortOrder();
            soId.add(scId);
            // Задание сортировка коллекции по дате регистрации(registration_date)
            SortCriterion scRegDate = new SortCriterion("registration_date", Order.ASCENDING);
            SortOrder soRegDate = new SortOrder();
            soRegDate.add(scRegDate);
            // Получаем все исходящие документы
            IdentifiableObjectCollection col = null;
            // с 1 по 10
            col = collectionService.findCollection("Outgoing_Document", soId, null, 0, PAGE_COUNT);
            // с 11 по 20
            col = collectionService.findCollection("Outgoing_Document", soId, null, 10, PAGE_COUNT);
            // с 21 по 30
            col = collectionService.findCollection("Outgoing_Document", soId, null, 20, PAGE_COUNT);
            // с 1 по 10 сортировка по рег. дате
            col = collectionService.findCollection("Outgoing_Document", soRegDate, null, 0, PAGE_COUNT);
            // Получаем случайный объект из коллекции
            randomInt = randomGenerator.nextInt(col.size());
            element = col.get(randomInt);
            //Получаем объект исходящего документа
            DomainObject outgoingDoc = crudService.find(element.getId());
            // Получаем поручения по исходящему документу
            // Фильтр по документу
            Filter filter = new Filter();
            filter.setFilter("byDoc");
            ReferenceValue lv = new ReferenceValue(outgoingDoc.getId());
            filter.addCriterion(0, lv);
            List<Filter> filters = new ArrayList<>();
            filters.add(filter);
            // Получаем все резолюции по документу
            IdentifiableObjectCollection colAssignment = collectionService.findCollection("Assignment", soId, filters);
            // Получаем случайную букву
            char c = (char) (randomGenerator.nextInt(26) + 'a');
            //Фильтр по первой букве
            Filter filterByLetter = new Filter();
            filterByLetter.setFilter("byFirstLetter");
           // StringValue sv = new StringValue( String.valueOf(c));
            StringValue sv = new StringValue("n");
            filterByLetter.addCriterion(0, sv);
            List<Filter> filters1 = new ArrayList<>();
            filters1.add(filterByLetter);
            //Получаем всех сотрудников на заданную букву
            IdentifiableObjectCollection colEmployees = null;
            // с 1 по 10
            colEmployees = collectionService.findCollection("Employees", soId, filters1, 0, PAGE_COUNT);
            // с 11 по 20
            colEmployees = collectionService.findCollection("Employees", soId, filters1, 10, PAGE_COUNT);
            // с 21 по 30
            colEmployees = collectionService.findCollection("Employees", soId, filters1, 20, PAGE_COUNT);
            //Все
            colEmployees = collectionService.findCollection("Employees", soId, filters1);
            // Получаем случайный объект из коллекции
            randomInt = randomGenerator.nextInt(colEmployees.size());
            element = colEmployees.get(randomInt);
            //Получаем объект сотрудника
            DomainObject employee = crudService.find(element.getId());
             //Создание резолюции
            DomainObject dobj = crudService.createDomainObject("Assignment");
            //Исполнитель
            dobj.setReference("Executor", employee.getId());
            //Ссылка на исходящий документ
            dobj.setReference("Parent_Document", element.getId());
            crudService.save(dobj);
           // res.setResponseData(dobj.toString(), null);
            //res.setDataType(SampleResult.TEXT);
            res.setSamplerData( dobj.toString());
            //res.setResponseMessage(dobj.toString());
            //res.setResponseOK();
            res.setSuccessful(true);
            res.setResponseCodeOK();
            res.setResponseMessageOK();
            //res.setResponseData("Object created: "+dobj.toString(), null);
            res.sampleEnd();
            ctx.close();

        } catch (Exception ex) {
            log.debug("", ex);
            res.setResponseMessage(ex.toString());
        }
        return res;
    }

    public void setProviderUrl(String value) {
        setProperty(PROVIDER_URL, value);
        setName(value);
    }

    public String getProviderUrl() {
        return getPropertyAsString(PROVIDER_URL);
    }

    public void setSecurityPrincipal(String value) {
        setProperty(SECURITY_PRINCIPAL, value);
        setName(value);
    }

    public String getSecurityPrincipal() {
        return getPropertyAsString(SECURITY_PRINCIPAL);
    }

    public void setSecurityCredentials(String value) {
        setProperty(SECURITY_CREDENTIALS, value);
        setName(value);
    }

    public String getSecurityCredentials() {
        return getPropertyAsString(SECURITY_CREDENTIALS);
    }


}
