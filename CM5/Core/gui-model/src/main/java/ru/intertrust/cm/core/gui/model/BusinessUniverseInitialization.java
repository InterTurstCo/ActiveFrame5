package ru.intertrust.cm.core.gui.model;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Базовая информация, необходимая при загрузке Business Universe
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 12:37
 */
public class BusinessUniverseInitialization implements Dto {
    private String currentLogin;
    private String firstName;
    private String lastName;
    private String eMail;

    public String getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(String currentLogin) {
        if (currentLogin == null){
            this.currentLogin = "";
        }
        this.currentLogin = currentLogin;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        if (eMail == null){
            this.eMail = "null@null.com";
        }

        this.eMail = eMail;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null){
            this.lastName = "";
        }
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null){
            this.firstName = "";
        }
        this.firstName = firstName;
    }

}
