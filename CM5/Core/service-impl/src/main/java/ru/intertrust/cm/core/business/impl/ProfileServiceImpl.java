package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.ProfileException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Stateless(name = "ProfileService")
@Local(ProfileService.class)
@Remote(ProfileService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    //private static final String DEFAULT_PROFILE_NAME = "admin";

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ConfigurationExplorer configurationService;

    //private Profile defaultProfile;

    /**
     * Получение профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     *
     * @param name имя профиля
     * @return
     */
    @Override
    public Profile getProfile(String name) {
        try {
            accessControlService.createAdminAccessToken(currentUserAccessor.getCurrentUser());

            Id profileId = findProfileByName(name);

            ProfileObject profileObject = new ProfileObject();
            profileObject.setId(profileId);
            profileObject.setName(name);
            if (profileId != null) {                
                IdentifiableObjectCollection profileValues = getProfileValues(profileId);
                fillProfileAttributes(profileObject, profileValues);
            }

            return profileObject;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getProfile", ex);
            throw new UnexpectedException("ProfileService", "getProfile", "name: " + name, ex);
        }
    }
/*
    private Profile getDefaultProfile() {
        if (defaultProfile == null) {
            defaultProfile = getProfile(DEFAULT_PROFILE_NAME);
        }
        return defaultProfile;
    }
*/
    private IdentifiableObjectCollection getProfileValues(Id profileId){
        //String query = "select pv.id from profile_value pv where pv.profile = {0}";
        String query = "select id from profile_value where profile = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new ReferenceValue(profileId));
        IdentifiableObjectCollection profileValues = collectionsService.findCollectionByQuery(query, params);
        return profileValues;
    }
    
    private Id findProfileByName(String name){
        String query = "select id from profile where name = {0}";
        List<Value> params = new ArrayList<Value>();
        params.add(new StringValue(name));
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, params);
        Id result = null;
        if (collection.size() > 0){
            result = collection.get(0).getId();
        }
        return result;        
    }
    
    
    /**
     * Получения профиля персоны. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования пользовательских профилей администраторами. При его вызове должен создаваться AdminAccessToken
     *
     * @param personId
     * @return
     */
    @Override
    public Profile getPersonProfile(Id personId) {
        try {
            accessControlService.createAdminAccessToken(currentUserAccessor.getCurrentUser());

            ProfileObject personProfileObject = new ProfileObject();
            DomainObject personDo = crudService.find(personId);
            Id profileId = personDo.getReference("profile");
            if (profileId == null) {
                personProfileObject.setName(personDo.getString("login"));
            }else{
                personProfileObject.setId(profileId);
                IdentifiableObjectCollection profileValues = getProfileValues(profileId);
                fillProfileAttributes(personProfileObject, profileValues);    
            }

            return personProfileObject;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonProfile", ex);
            throw new UnexpectedException("ProfileService", "getPersonProfile", "personId: " + personId, ex);
        }
    }


    /**
     * Сохранения профиля системы. Профиль содержит данные профиля без учета иерархии профилей. Предназначен для
     * редактирования системных профилей администраторами при его вызове должен создаваться AdminAccessToken
     */
    @Override
    public void setProfile(Profile profile) {
        try {
            accessControlService.createAdminAccessToken(currentUserAccessor.getCurrentUser());

            Id profileId = profile.getId();

            DomainObject profileDomainObject = null;

            if (profileId == null) {
                // create new profile record
                profileDomainObject = crudService.createDomainObject("system_profile");
            } else {
                profileDomainObject = crudService.find(profileId);
                // profile already exists - clean existing attributes
                cleanProfileAttributes(profileId);
            }

            // save profile DO
            String name = profile.getName();
            if (name == null || name.length() == 0){
                throw new ProfileException("System profile name can't be empty");
            }
            profileDomainObject.setString("name", name);
            profileDomainObject.setReference("parent", profile.getParent());
            profileDomainObject = crudService.save(profileDomainObject);
            profileId = profileDomainObject.getId();

            // save attributes
            ArrayList<String> attributeNames = profile.getFields();
            if (attributeNames != null) {
                for (String attributeName : attributeNames) {
                    saveProfileAttribute(profile, profileId, attributeName);
                }
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setProfile", ex);
            throw new UnexpectedException("ProfileService", "setProfile", "profile: " + profile, ex);
        }

    }


    /**
     * Получение пользовательского профиля. Профиль содержит данные профиля пользователя с учетом иерархии профилей.
     * Предназначен для работы под провами простого пользователя
     *
     * @return
     */
    @Override
    public Profile getPersonProfile() {
        try {
            Id currentUserId = currentUserAccessor.getCurrentUserId();
            return currentUserId != null ? getPersonProfileByPersonId(currentUserId) : null;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonProfile", ex);
            throw new UnexpectedException("ProfileService", "getPersonProfile", "", ex);
        }

    }

    /**
     * Получение пользовательского профиля. Профиль содержит данные профиля пользователя с учетом иерархии профилей.
     * Предназначен для работы под провами простого пользователя
     * @return
     */
    @Override
    public Profile getPersonProfileByPersonId(Id personId) {
        try {
            ProfileObject personProfileObject = new ProfileObject();
            DomainObject personDo = crudService.find(personId);
            Id profileId = personDo.getReference("profile");
            if (profileId == null) {
                personProfileObject.setName(personDo.getString("login"));
            }else{
                personProfileObject.setId(profileId);
                IdentifiableObjectCollection profileValues = getProfileValues(profileId);
                fillProfileAttributes(personProfileObject, profileValues);    
                fillInheritedAttributes(personProfileObject, personProfileObject);
            }

            return personProfileObject;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonProfileByPersonId", ex);
            throw new UnexpectedException("ProfileService", "getPersonProfileByPersonId", "personId: " + personId, ex);
        }
    }

    @Override
    public void setPersonProfile(Profile profile) {
        try {
            Id profileId = profile.getId();

            DomainObject personProfileDo = null;

            if (profileId == null) {
                // create new profile record
                personProfileDo = crudService.createDomainObject("person_profile");
                personProfileDo.setString("name", profile.getName());
            } else {
                personProfileDo = crudService.find(profileId);
                // profile already exists - clean existing attributes
                cleanProfileAttributes(profileId);
            }

            // Set parent profile
            Id parentProfileId = profile.getParent();
            if ((parentProfileId == null && personProfileDo.getReference("parent") != null) ||
                    (parentProfileId != null && !parentProfileId.equals(personProfileDo.getReference("parent")))){
                personProfileDo.setReference("parent", parentProfileId);
            }

            // save person profile DO
            if (personProfileDo.isNew() || personProfileDo.isDirty()){
                personProfileDo = crudService.save(personProfileDo);
            }
            
            // Если это новый провиль то обновляем обьект персоны
            if (profileId == null){
                profileId = personProfileDo.getId();
                Id currentUserId = currentUserAccessor.getCurrentUserId();
                DomainObject person = crudService.find(currentUserId);
                person.setReference("profile", profileId);
                crudService.save(person);
            }

            // read parent and inherited profile attributes
            ProfileObject parentProfile = new ProfileObject();
            if (parentProfileId != null){
                fillProfileAttributes(parentProfile, getProfileValues(parentProfileId));
                fillInheritedAttributes(parentProfile, parentProfile);
            }

            // save only overridden attributes
            ArrayList<String> attributeNames = profile.getFields();
            HashSet<String> parentProfileAttributeNames = new HashSet<>(parentProfile.getFields());
            if (attributeNames != null) {
                for (String attributeName : attributeNames) {
                    ProfileValue profileValue = (ProfileValue) profile.getValue(attributeName);
                    if (parentProfileAttributeNames.contains(attributeName)){
                        ProfileValue parentProfileValue = (ProfileValue) parentProfile.getValue(attributeName);
                        if (parentProfileValue.isReadOnly()){
                            continue;
                        }

                        if (profileValue.equals(parentProfileValue)){
                            continue;
                        }
                    }

                    saveProfileAttribute(profile, profileId, attributeName);
                }
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setPersonProfile", ex);
            throw new UnexpectedException("ProfileService", "setPersonProfile", "profile: " + profile, ex);
        }
    }

    @Override
    public String getPersonLocale() {
        try {
            Profile profile = null;
            try {
                profile = getPersonProfile();
            } catch (Exception e) {
                logger.debug("Exception caught while fetching current user's profile", e);
                // Локаль пользователя будет получена иным путём; пропускаем
            }
            if (profile != null) {
                final String locale = profile.getString(ProfileService.LOCALE);
                if (locale != null && MessageResourceProvider.getAvailableLocales().contains(locale)) {
                    return locale;
                }
            }
            final DefaultLocaleConfig defaultLocaleConfig = configurationService.getGlobalSettings().getDefaultLocaleConfig();
            return defaultLocaleConfig != null ? defaultLocaleConfig.getName() : MessageResourceProvider.getDefaultLocale();
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonLocale", ex);
            throw new UnexpectedException("ProfileService getPersonLocale", ex);
        }
    }

    @Override
    public void setPersonLocale(String locale) {
        Profile profile = getPersonProfile();
        profile.setValue(ProfileService.LOCALE, new ProfileStringValue(locale));
        setPersonProfile(profile);
    }

    private void cleanProfileAttributes(Id profileId) {
        IdentifiableObjectCollection profileValues = getProfileValues(profileId);
        if (profileValues.size() > 0) {
            for (IdentifiableObject profileValueObj : profileValues) {
                Id profileValueId = profileValueObj.getId();
                crudService.delete(profileValueId);
            }
        }
    }

    /*private IdentifiableObjectCollection getProfileValuesByProfileId(Id profileId) {
        Filter filter = new Filter();
        filter.setFilter("byId");
        filter.addReferenceCriterion(0, profileId);

        return collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
    }*/

    /*private IdentifiableObjectCollection getProfileValuesByPersonId(Id personId) {
        Filter filter = new Filter();
        filter.setFilter("byPersonId");
        filter.addReferenceCriterion(0, personId);

        return collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
    }*/

    private void fillProfileAttributes(ProfileObject profileObject, IdentifiableObjectCollection profileValues) {

        if (profileValues != null) {
            for (IdentifiableObject profileValueObj : profileValues) {
                Id profileValueId = profileValueObj.getId();
                DomainObject domainObject = crudService.find(profileValueId);
                String key = domainObject.getString("key");
                Boolean readonly = domainObject.getBoolean("readonly");
                Id profile = domainObject.getReference("profile");
                profileObject.setId(profile);
                Value value = domainObject.getValue("value");
                ProfileValue profileValue = null;
                if (value instanceof StringValue) {
                    profileValue = new ProfileStringValue(((StringValue) value).get());
                } else if (value instanceof LongValue) {
                    profileValue = new ProfileLongValue(((LongValue) value).get());
                } else if (value instanceof BooleanValue) {
                    profileValue = new ProfileBooleanValue(((BooleanValue) value).get());
                } else if (value instanceof DateTimeValue) {
                    profileValue = new ProfileDateTimeValue(((DateTimeValue) value).get());
                } else if (value instanceof ReferenceValue) {
                    profileValue = new ProfileReferenceValue(((ReferenceValue) value).get());
                } else {
                    throw new ProfileException("Unsupported profile value: " + value);
                }
                profileValue.setReadOnly(readonly);
                profileObject.setValue(key, (Value) profileValue);
            }
        }

        DomainObject profileDomainObject = crudService.find(profileObject.getId());
        Id parent = profileDomainObject.getReference("parent");
        profileObject.setParent(parent);

    }

    private void fillInheritedAttributes(ProfileObject result, ProfileObject current) {
        if (current.getParent() == null) return;

        ProfileObject parentProfileObject = new ProfileObject();
        DomainObject parentProfileDo = crudService.find(current.getParent());
        parentProfileObject.setId(parentProfileDo.getId());
        fillProfileAttributes(parentProfileObject, getProfileValues(parentProfileDo.getId()));

        HashSet<String> personFields = new HashSet<>(result.getFields());
        ArrayList<String> parentFields = parentProfileObject.getFields();

        if (parentFields != null) {
            for (String parentField : parentFields) {
                if (!personFields.contains(parentField)){
                    Value inheritedValue = parentProfileObject.getValue(parentField);
                    result.setValue(parentField, inheritedValue);
                }
            }
        }

        fillInheritedAttributes(result, parentProfileObject);

    }

    private void saveProfileAttribute(IdentifiableObject profile, Id profileId, String attributeName) {
        DomainObject pvDomainObject = null;
        ProfileValue profileValue = (ProfileValue) profile.getValue(attributeName);
        Value value = profile.getValue(attributeName);
        if (profileValue instanceof ProfileStringValue) {
            pvDomainObject = crudService.createDomainObject("profile_value_string");
        } else if (profileValue instanceof ProfileLongValue) {
            pvDomainObject = crudService.createDomainObject("profile_value_long");
        } else if (profileValue instanceof ProfileBooleanValue) {
            pvDomainObject = crudService.createDomainObject("profile_value_boolean");
        } else if (profileValue instanceof ProfileDateTimeValue) {
            pvDomainObject = crudService.createDomainObject("profile_value_date");
        } else if (profileValue instanceof ProfileReferenceValue) {
            String type = findProfileReferenceType(((ProfileReferenceValue) profileValue).get());
            pvDomainObject = crudService.createDomainObject(type);
        } else {
            throw new ProfileException("Unsupported profile profileValue: " + profileValue);
        }

        pvDomainObject.setString("key", attributeName);
        pvDomainObject.setReference("profile", profileId);
        pvDomainObject.setBoolean("readonly", profileValue.isReadOnly());
        pvDomainObject.setValue("value", value);

        crudService.save(pvDomainObject);
    }

    private String findProfileReferenceType(Id id){
        DomainObject domainObject = crudService.find(id);
        String typeName = domainObject.getTypeName();

        Collection<DomainObjectTypeConfig> profileValueTypeConfig = configurationService.findChildDomainObjectTypes("profile_value", true);
        for (DomainObjectTypeConfig domainObjectTypeConfig : profileValueTypeConfig) {
            List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getFieldConfigs();
            for (FieldConfig fieldConfig : fieldConfigs) {
                if ("value".equals(fieldConfig.getName()) && fieldConfig.getFieldType().equals(FieldType.REFERENCE)){
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    if (typeName.equals( referenceFieldConfig.getType())) {
                        return domainObjectTypeConfig.getName();
                    }
                }
            }
        }

        throw new ProfileException("Unknown profile reference attribute type " + typeName);

    }

}
