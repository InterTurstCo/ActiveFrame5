package ru.intertrust.cm.core.business.api.dto;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 12:51
 */
public class UserUidWithPassword implements UserCredentials {
    private static final long serialVersionUID = 311834124955492612L;

    private String userUid;
    private String password;
    private String clientTimeZone;

    public UserUidWithPassword() {
    }

    public UserUidWithPassword(final String userUid, final String password) {
        this.userUid = userUid;
        this.password = password;
    }

    @Deprecated
    public UserUidWithPassword(final String userUid, final String password, final String clientTimeZone) {
        this.userUid = userUid;
        this.password = password;
        this.clientTimeZone = clientTimeZone;
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

    @Deprecated
    public String getClientTimeZone() {
        return clientTimeZone;
    }

    @Deprecated
    public void setClientTimeZone(String clientTimeZone) {
        this.clientTimeZone = clientTimeZone;
    }
}
