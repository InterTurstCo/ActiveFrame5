package ru.intertrust.cm.core.business.api.access;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserInfo {

    private String unid;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enable;
    private Map<String, List<String>> attributes;
    private List<String> requiredActions;
    private String temporaryPassword;

    public String getUnid() {
        return unid;
    }

    public void setUnid(String unid) {
        this.unid = unid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;
        final UserInfo userInfo = (UserInfo) o;
        return isEnable() == userInfo.isEnable() && Objects.equals(getUnid(), userInfo.getUnid())
                && Objects.equals(getUsername(), userInfo.getUsername())
                && Objects.equals(getEmail(), userInfo.getEmail())
                && Objects.equals(getFirstName(), userInfo.getFirstName())
                && Objects.equals(getLastName(), userInfo.getLastName())
                && Objects.equals(getAttributes(), userInfo.getAttributes())
                && Objects.equals(getRequiredActions(), userInfo.getRequiredActions())
                && Objects.equals(getTemporaryPassword(), userInfo.getTemporaryPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnid(), getUsername(), getEmail(), getFirstName(),
                getLastName(), isEnable(), getAttributes(), getRequiredActions(), getTemporaryPassword());
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "unid='" + unid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", enable=" + enable +
                ", attributes=" + attributes +
                ", requiredActions=" + requiredActions +
                '}';
    }
}
