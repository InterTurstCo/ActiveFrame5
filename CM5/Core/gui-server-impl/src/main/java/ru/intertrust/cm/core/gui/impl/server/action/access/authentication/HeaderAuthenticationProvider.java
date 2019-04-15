package ru.intertrust.cm.core.gui.impl.server.action.access.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUid;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.gui.api.server.authentication.AuthenticationProvider;

public class HeaderAuthenticationProvider implements AuthenticationProvider {
    
    @Value("${af5.header.authentication.value:}")
    private String headerNames;
    @Value("${af5.header.authentication.alt.uid.type:}")
    private String altUidType;

    @Autowired
    private PersonServiceDao personService;

    @Override
    public String getLoginPage() {
        return null;
    }

    @Override
    public String getLoginImageUrl() {
        return null;
    }

    @Override
    public UserCredentials login(HttpServletRequest request, HttpServletResponse response) {
        UserCredentials result = null;

        if (headerNames != null && !headerNames.isEmpty()) {
            String[] headerNameArr = headerNames.split("[;, ]");
            String headerValue = null;
            for (String headerName : headerNameArr) {
                if (request.getHeader(headerName) != null) {
                    headerValue = request.getHeader(headerName);
                    break;
                }
            }

            if (headerValue != null) {
                result = new UserUid(getPersonUid(headerValue));
            }
        }

        return result;
    }

    protected String getPersonUid(String headerValue) {
        // Проверяем что такой пользователь существует
        DomainObject person = null;
        try {
            person = personService.findPersonByLogin(headerValue);
        }catch (IllegalArgumentException ignoreEx) {
            // Не найден такой пользователь
        }
        
        // В заголовке могут быть альтернативные имена. Проверяем на то что пользователь есть
        if (person == null && altUidType != null && !altUidType.isEmpty())  {
            person = personService.findPersonByAltUid(headerValue, altUidType);
        }
        
        String result = null;
        if(person != null) {
            result = person.getString("login");
        }
        
        return result;
    }
}
