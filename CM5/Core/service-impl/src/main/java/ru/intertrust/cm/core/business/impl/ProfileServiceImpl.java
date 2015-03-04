package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ProfileException;
import ru.intertrust.cm.core.model.UnexpectedException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Stateless(name = "ProfileService")
@Local(ProfileService.class)
@Remote(ProfileService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private ConfigurationService configurationService;

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

            Filter filter = new Filter();
            filter.setFilter("byName");
            filter.addStringCriterion(0, name);

            IdentifiableObjectCollection profileValues = collectionsService.findCollection("ProfileValues",
                    new SortOrder(), Collections.singletonList(filter));
            if (profileValues.size() == 0) {
                ProfileObject profileObject = new ProfileObject();
                profileObject.setName(name);
                return profileObject;
            }

            ProfileObject profileObject = new ProfileObject();
            fillProfileAttributes(profileObject, profileValues);
            profileObject.setName(name);

            return profileObject;
        } catch (AccessException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getProfile", ex);
            throw new UnexpectedException("ProfileService", "getProfile", "name: " + name, ex);
        }
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

            IdentifiableObjectCollection profileValues = getProfileValuesByPersonId(personId);
            if (profileValues.size() == 0) {
                return new ProfileObject();
            }

            ProfileObject profileObject = new ProfileObject();
            fillProfileAttributes(profileObject, profileValues);

            return profileObject;
        } catch (AccessException ex) {
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
        } catch (AccessException | ProfileException ex) {
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
    public PersonProfile getPersonProfile() {

        try {
            Id currentUserId = currentUserAccessor.getCurrentUserId();
            return getPersonProfileByPersonId(currentUserId);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonProfile", ex);
            throw new UnexpectedException("ProfileService", "getPersonProfile", "", ex);
        }

    }

    @Override
    public PersonProfile getPersonProfileByPersonId(Id personId) {
        try {
            PersonProfileObject personProfileObject = new PersonProfileObject();
            DomainObject personDo = crudService.find(personId);
            Id profileId = personDo.getReference("profile");
            if (profileId == null) {
                return personProfileObject;
            }

            personProfileObject.setId(profileId);
            IdentifiableObjectCollection profileValues = getProfileValuesByPersonId(personId);
            fillProfileAttributes(personProfileObject, profileValues);

            fillInheritedAttributes(personProfileObject, personProfileObject);

            return personProfileObject;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getPersonProfileByPersonId", ex);
            throw new UnexpectedException("ProfileService", "getPersonProfileByPersonId", "personId: " + personId, ex);
        }
    }

    @Override
    public void setPersonProfile(PersonProfile profile) {

        try {
            Id profileId = profile.getId();

            DomainObject personProfileDo = null;

            if (profileId == null) {
                // create new profile record
                personProfileDo = crudService.createDomainObject("person_profile");
            } else {
                personProfileDo = crudService.find(profileId);
                // profile already exists - clean existing attributes
                cleanProfileAttributes(profileId);
            }

            // save person profile DO
            Id parentProfileId = profile.getParent();
            personProfileDo.setReference("parent", parentProfileId);
            personProfileDo = crudService.save(personProfileDo);
            profileId = personProfileDo.getId();

            // read parent and inherited profile attributes
            BaseProfileObject parentProfile = new ProfileObject();
            if (parentProfileId != null){
                fillProfileAttributes(parentProfile, getProfileValuesByProfileId(parentProfileId));
                fillInheritedAttributes(parentProfile, parentProfile);
            }

            // save only overridden attributes
            ArrayList<String> attributeNames = profile.getFields();
            ArrayList<String> parentProfileAttributeNames = parentProfile.getFields();
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
        } catch (AccessException | ProfileException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in setPersonProfile", ex);
            throw new UnexpectedException("ProfileService", "setPersonProfile", "profile: " + profile, ex);
        }
    }

    @Override
    public String getPersonLocale() {
        PersonProfile profile = getPersonProfile();
        if (profile != null && profile.getString(ProfileService.LOCALE) != null) {
            return profile.getString(ProfileService.LOCALE);
        }
        if (configurationService.getGlobalSettings().getDefaultLocaleConfig() != null) {
            return configurationService.getGlobalSettings().getDefaultLocaleConfig().getName();
        }
        return null;
    }

    @Override
    public void setPersonLocale(String locale) {
        PersonProfile profile = getPersonProfile();
        profile.setString(ProfileService.LOCALE, locale);
        //setPersonProfile(profile); //FIXME: uncomment after CMFIVE-3397 is fixed.
    }

    private void cleanProfileAttributes(Id profileId) {
        IdentifiableObjectCollection profileValues = getProfileValuesByProfileId(profileId);
        if (profileValues.size() > 0) {
            for (IdentifiableObject profileValueObj : profileValues) {
                Id profileValueId = profileValueObj.getId();
                crudService.delete(profileValueId);
            }
        }
    }

    private IdentifiableObjectCollection getProfileValuesByProfileId(Id profileId) {
        Filter filter = new Filter();
        filter.setFilter("byId");
        filter.addReferenceCriterion(0, profileId);

        return collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
    }

    private IdentifiableObjectCollection getProfileValuesByPersonId(Id personId) {
        Filter filter = new Filter();
        filter.setFilter("byPersonId");
        filter.addReferenceCriterion(0, personId);

        return collectionsService.findCollection("ProfileValues",
                new SortOrder(), Collections.singletonList(filter));
    }

    private void fillProfileAttributes(BaseProfileObject profileObject, IdentifiableObjectCollection profileValues) {

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

    private void fillInheritedAttributes(BaseProfileObject result, BaseProfileObject current) {
        if (current.getParent() == null) return;

        ProfileObject parentProfileObject = new ProfileObject();
        DomainObject parentProfileDo = crudService.find(current.getParent());
        parentProfileObject.setId(parentProfileDo.getId());
        fillProfileAttributes(parentProfileObject, getProfileValuesByProfileId(parentProfileDo.getId()));

        ArrayList<String> personFields = result.getFields();
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
