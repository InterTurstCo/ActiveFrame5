package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.model.ProfileException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.Collections;

@Stateless(name = "ProfileService")
@Local(ProfileService.class)
@Remote(ProfileService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ProfileServiceImpl implements ProfileService {


    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    /**
     * Получение профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     * @param name имя профиля
     * @return
     */
    @Override
    public Profile getProfile(String name) {
        //todo check admin privileges

        Filter filter = new Filter();
        filter.setFilter("byName");
        filter.addStringCriterion(0, name);

        IdentifiableObjectCollection profileValues = collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
        if (profileValues.size() == 0) {
            throw new ProfileException("System Profile not found for name = " + name);
        }

        ProfileObject profileObject = makeProfileObject(profileValues);


        return profileObject;
    }

    /**
     * Получения профиля персоны. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования пользовательских профилей администраторами. При его вызове должен создаваться AdminAccessToken
     * @param personId
     * @return
     */
    @Override
    public Profile getPersonProfile(Id personId) {
        //todo check admin privileges

        Filter filter = new Filter();
        filter.setFilter("byPersonId");
        filter.addReferenceCriterion(0, personId);

        IdentifiableObjectCollection profileValues = collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
        if (profileValues.size() == 0) {
            throw new ProfileException("Person Profile not found for personId = " + personId);
        }

        ProfileObject profileObject = makeProfileObject(profileValues);


        return profileObject;
    }

    private ProfileObject makeProfileObject(IdentifiableObjectCollection profileValues) {
        ProfileObject profileObject = new ProfileObject();

        for (IdentifiableObject profileValueObj : profileValues) {
            Id profileValueId = profileValueObj.getId();
            DomainObject domainObject = crudService.find(profileValueId);
            String key = domainObject.getString("key");
            Boolean readonly = domainObject.getBoolean("readonly");
            Id profile = domainObject.getReference("profile");
            profileObject.setId(profile);
            Value value = domainObject.getValue("value");
            ProfileValue profileValue = null;
            if (value instanceof StringValue){
                profileValue = new ProfileStringValue(((StringValue)value).get());
            } else if (value instanceof LongValue){
                profileValue = new ProfileLongValue(((LongValue)value).get());
            } else if (value instanceof BooleanValue){
                profileValue = new ProfileBooleanValue(((BooleanValue)value).get());
            } else if (value instanceof DateTimeValue){
                profileValue = new ProfileDateTimeValue(((DateTimeValue)value).get());
            } else if (value instanceof ReferenceValue){
                profileValue = new ProfileReferenceValue(((ReferenceValue)value).get());
            } else {
                throw new ProfileException("Unsupported profile value: " + value);
            }
            profileValue.setReadOnly(readonly);
            profileObject.setValue(key, (Value)profileValue);
        }
        return profileObject;
    }

    /**
     * Сохранения профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     */
    @Override
    public void setProfile(Profile profile) {

    }

    @Override
    public PersonProfile getPersonProfile() {
        return null;
    }

    @Override
    public void setPersonProfile(PersonProfile profile) {

    }
}
