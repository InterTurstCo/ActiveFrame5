package ru.intertrust.cm.core.business.api.dto;

/**
 * Пользователь системы и роль.
 *
 * @author atsvetkov
 *
 */
public class AuthenticationInfoAndRole {

    private int id;

    private String userUid;

    private String Role;

    private String password;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }



}
