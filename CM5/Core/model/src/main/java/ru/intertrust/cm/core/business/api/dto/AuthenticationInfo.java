package ru.intertrust.cm.core.business.api.dto;

/**
 * Пользователь системы.
 * 
 * @author atsvetkov
 * 
 */
public class AuthenticationInfo {

    private int id;
    
    private String userUid;
    
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
    
}
