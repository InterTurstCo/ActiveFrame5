package ru.intertrust.cm.core.business.api.dto;

/**
 * Имплементация информации по пользователе
 * @author larin
 *
 */
public class UserUid implements UserCredentials {
    private static final long serialVersionUID = 311834124955492612L;

    private String userUid;

    public UserUid() {
    }

    public UserUid(final String userUid) {
        this.userUid = userUid;
    }

    @Override
    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userUid == null) ? 0 : userUid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserUid other = (UserUid) obj;
        if (userUid == null) {
            if (other.userUid != null)
                return false;
        } else if (!userUid.equals(other.userUid))
            return false;
        return true;
    }
}