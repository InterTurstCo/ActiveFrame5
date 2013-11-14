package ru.intertrust.cm.core.gui.impl.client;

/**
 * Created with IntelliJ IDEA.
 * User: tbilyi
 * Date: 14.11.13
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class CurrentUserInfo {
    private String currentLogin;
    private String firstName;
    private String lastName;
    private String mail;

    public CurrentUserInfo(String currentLogin, String firstName, String lastName, String mail) {
        this.currentLogin = currentLogin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
    }

    public String getCurrentLogin() {
        if(this.currentLogin == null ){
            currentLogin = "";
        }
        return currentLogin;
    }

    public void setCurrentLogin(String currentLogin) {
        this.currentLogin = currentLogin;
    }

    public String getFirstName() {
        if(this.firstName == null ){
            firstName = "";
        }
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        if(this.lastName == null ){
            lastName = "";
        }
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMail() {
        if(this.mail == null ){
            mail = "";
        }
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
