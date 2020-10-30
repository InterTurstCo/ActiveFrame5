package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.2015
 *         Time: 15:18
 */
public class AbsentDomainObject implements DomainObject, Cloneable {
    public static final DomainObject INSTANCE = new AbsentDomainObject();

    AbsentDomainObject() {
    }

    @Override
    public String getTypeName() {
        return null;
    }

    @Override
    public Date getCreatedDate() {
        return null;
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Id getCreatedBy() {
        return null;
    }

    @Override
    public Id getModifiedBy() {
        return null;
    }

    /**
     * Возвращает идентификатор объекта, по которому определяются права на данный объект
     *
     * @return идентификатор объекта, по которому определяются права на данный объект
     */
    @Override
    public Id getAccessObjectId() {
        return null;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public Id getStatus() {
        return null;
    }

    @Override
    public Id getStamp() {
        return null;
    }

    @Override
    public boolean isAbsent() {
        return true;
    }

    @Override
    public Id getId() {
        return null;
    }

    @Override
    public void setId(Id id) {

    }

    @Override
    public void setValue(String field, Value value) {

    }

    @Override
    public <T extends Value> T getValue(String field) {
        return null;
    }

    @Override
    public ArrayList<String> getFields() {
        return null;
    }

    @Override
    public void setString(String field, String value) {

    }

    @Override
    public String getString(String field) {
        return null;
    }

    @Override
    public void setLong(String field, Long value) {

    }

    @Override
    public Long getLong(String field) {
        return null;
    }

    @Override
    public void setBoolean(String field, Boolean value) {

    }

    @Override
    public Boolean getBoolean(String field) {
        return null;
    }

    @Override
    public void setDecimal(String field, BigDecimal value) {

    }

    @Override
    public BigDecimal getDecimal(String field) {
        return null;
    }

    @Override
    public void setTimestamp(String field, Date value) {

    }

    @Override
    public Date getTimestamp(String field) {
        return null;
    }

    @Override
    public void setTimelessDate(String field, TimelessDate value) {

    }

    @Override
    public TimelessDate getTimelessDate(String field) {
        return null;
    }

    @Override
    public void setDateTimeWithTimeZone(String field, DateTimeWithTimeZone value) {

    }

    @Override
    public DateTimeWithTimeZone getDateTimeWithTimeZone(String field) {
        return null;
    }

    @Override
    public void setReference(String field, DomainObject domainObject) {

    }

    @Override
    public void setReference(String field, Id id) {

    }

    @Override
    public Id getReference(String field) {
        return null;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean containsFieldValues(Map<String, Value> fieldValues) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ((DomainObject) obj).isAbsent()) {
            return true;
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return this == INSTANCE ? this : super.clone();
    }
}
