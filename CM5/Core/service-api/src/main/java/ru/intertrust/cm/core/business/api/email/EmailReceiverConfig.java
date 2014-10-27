package ru.intertrust.cm.core.business.api.email;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@Root(name="email-receiver-config")
public class EmailReceiverConfig implements ScheduleTaskParameters{
    private static final long serialVersionUID = -4121306482247861648L;

    @Attribute(name="host", required=true)
    private String host;
    @Attribute(name="port", required=false)
    private Integer port;
    @Attribute(name="login", required=true)
    private String login;
    @Attribute(name="password", required=true)
    private String password;
    @Attribute(name="protocol", required=false)
    private EmailReceiverProtocol protocol;
    @Attribute(name="encryption-type", required=false)
    private EmailReceiverEncryptionType encryptionType;
    @Attribute(name="max-messages", required=false)
    private Integer maxMessages;
    
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public EmailReceiverProtocol getProtocol() {
        return protocol;
    }
    public void setProtocol(EmailReceiverProtocol protocol) {
        this.protocol = protocol;
    }
    public EmailReceiverEncryptionType getEncryptionType() {
        return encryptionType;
    }
    public void setEncryptionType(EmailReceiverEncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public Integer getMaxMessages() {
        return maxMessages;
    }
    public void setMaxMessages(Integer maxMessages) {
        this.maxMessages = maxMessages;
    }
    
    public enum EmailReceiverProtocol{
        pop3, imap
    }

    public enum EmailReceiverEncryptionType{
        ssl, tls
    }
    
}
