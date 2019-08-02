package ru.intertrust.cm.nbrbase.gui.interceptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveHashMap;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.api.server.form.FormAfterSaveInterceptor;
import ru.intertrust.cm.core.gui.api.server.form.FormBeforeSaveInterceptor;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.model.ObjectNotFoundException;


@ComponentName("person.save.handler")
public class PersonFormSaveInterceptor implements FormAfterSaveInterceptor , FormBeforeSaveInterceptor {
    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;
    
    @Override
    public void beforeSave(FormState formState)  {
        DomainObject rootDomainObject =  formState.getObjects().getRootNode().getDomainObject();
        final String LastNameField = ((TextState) formState.getWidgetState("LastNameField")).getText();
        final String FirstNameField = ((TextState) formState.getWidgetState("FirstNameField")).getText();
        final String LoginField = ((TextState) formState.getWidgetState("LoginField")).getText();
        final String auth_info = ((TextState) formState.getWidgetState("auth_info")).getText();

        if (LastNameField == null || LastNameField.isEmpty()) {
            throw new GuiException("Заполните поле \"Фамилия\"");
        }
        if (FirstNameField == null || FirstNameField.isEmpty()) {
            throw new GuiException("Заполните поле \"Имя\"");
        }
        if (LoginField == null || LoginField.isEmpty()) {
            throw new GuiException("Заполните поле \"Логин / AD-имя\"");
        }

        final String ssoSettings = applicationContext.getEnvironment().getProperty("af5.authentication.type");
        boolean isSSO = false;
        if (ssoSettings!=null) {
            isSSO = ssoSettings.toLowerCase().contains("headerauthentication");
        }

        if (isSSO) {
            if (auth_info != null && !auth_info.isEmpty()) {
                if (ssoSettings.equalsIgnoreCase("headerauthentication")) {
                    ((TextState) formState.getWidgetState("auth_info")).setText("");
                }
            }
        } else {
            if (auth_info == null || auth_info.isEmpty()) {
                throw new GuiException("Заполните поле \"Пароль\"");
            }
        }

        final List<Value> paramForName = new ArrayList<>();
        String searchQuery = "select id from person where lower(login) = {0}";
        paramForName.add(new StringValue(LoginField.toLowerCase()));
        if (!rootDomainObject.isNew()) {
            paramForName.add(new ReferenceValue(rootDomainObject.getId()));
            searchQuery += " and id <> {1}";
        }

        IdentifiableObjectCollection coll = this.collectionsService.findCollectionByQuery(searchQuery, paramForName, 0, 1);

        if (coll.size() > 0) {
            throw new GuiException("Уже существует пользователь с таким значением \"Логин / AD-имя\"");
        }

        if ("Employee".equalsIgnoreCase(rootDomainObject.getTypeName())) {
            rootDomainObject.setString("name", LastNameField + " " + FirstNameField);
        }


        Map<String, Value>map = new HashMap<>();
        map.put("name", new StringValue("Active"));
        DomainObject statusActive = crudService.findByUniqueKey("status", map);

        if (rootDomainObject.getReference("status") != null) {
            if(!rootDomainObject.getReference("status").equals(statusActive.getId())) {
                if (accessControlService != null && domainObjectDao != null) {
                    AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
                    rootDomainObject = domainObjectDao.setStatus(rootDomainObject.getId(), statusActive.getId(), accessToken);
                    domainObjectDao.save(rootDomainObject, accessToken);
                } else {
                    throw new GuiException("Ошибка инициализации объектов доступа");
                }
            }
        }
    }

    @Override
    public DomainObject afterSave(FormState formState, CaseInsensitiveHashMap<WidgetConfig> widgetConfigById) {

        DomainObject auth_info = null;
        boolean isChanged = false;
        List<String> list = new ArrayList();

        DomainObject rootDomainObject =  formState.getObjects().getRootNode().getDomainObject();
        String login = rootDomainObject.getString("Login") ;

        String password = ((TextState) formState.getWidgetState("auth_info")).getText();

        auth_info = getAuth_infoDObject(login);

        auth_info.setString("User_Uid", login);
        auth_info.setString("Password", password);
        crudService.save( auth_info);

       // return crudService.save(rootDomainObject);
        return rootDomainObject;
    }


    private DomainObject findPersonByKey (String fieldName, String fieldValue){
        DomainObject person=null;
        try {
            Map<String, Value> paramsSimpleKey = new HashMap<>();
            paramsSimpleKey.put(Case.toLower(fieldName), new StringValue(fieldValue));
            person = crudService.findByUniqueKey("Person", paramsSimpleKey);
        } catch (ObjectNotFoundException e){
        }
        return person;
    }

    private DomainObject getAuth_infoDObject (String login){
        DomainObject auth_info = null;
        try {
            Map<String, Value> paramsSimpleKey = new HashMap<>();
            paramsSimpleKey.put("user_uid", new StringValue(login));
            auth_info = crudService.findByUniqueKey("Authentication_Info", paramsSimpleKey);
            if (auth_info == null){
                auth_info = crudService.createDomainObject("Authentication_Info");
            }
        } catch (ObjectNotFoundException e){
            //            throw new GuiException("Authentication_Info - ObjectNotFound");
        }
        return auth_info;
    }

    private List<String> listFieldsModification (FormState formState, DomainObject person, DomainObject  auth_info){
        List<String> list = new ArrayList();
        if  (person== null)  return list;

        //        String login = ((TextState) formState.getWidgetState("Login")).getText();
        //        String email = ((TextState) formState.getWidgetState("Email")).getText();
        String password = ((TextState) formState.getWidgetState("auth_info")).getText();
        //        String firstName =   ((TextState) formState.getWidgetState("7")).getText();
        //        String lastName = ((TextState) formState.getWidgetState("5")).getText();

        //        if (login==null) login="";
        //        if (email==null) email="";
        if (password==null) password="";
        //        if (firstName==null) firstName="";
        //        if (lastName==null) lastName="";

        //        if (!login.equals(person.getString("Login")))  list.add("Login");
        //        if (!email.equals(person.getString("Email")))  list.add("Email");
        //        if (!firstName.equals(person.getString("FirstName")))  list.add("FirstName");
        //        if (!lastName.equals(person.getString("LastName")))  list.add("LastName");
        if (auth_info!=null)
            if (!password.equals(auth_info.getString("Password")))  list.add("Password");

        return list;
    }


}
