package ru.intertrust.cm.core.business.api.dto;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 12:51
 */
public class UserUidWithPassword implements UserCredentials {
    private String userUid;
    private String password;

    public UserUidWithPassword() {
    }

    public UserUidWithPassword(String userUid, String password) {
        this.userUid = userUid;
        this.password = password;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
