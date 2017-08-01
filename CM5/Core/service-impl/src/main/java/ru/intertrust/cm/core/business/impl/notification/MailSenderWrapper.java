package ru.intertrust.cm.core.business.impl.notification;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Обертка для @{see JavaMailSenderImpl}.
 * @author atsvetkov
 *
 */
public class MailSenderWrapper extends JavaMailSenderImpl {
    public final String ENCRYPTION_TYPE_SSL = "ssl";
    public final String ENCRYPTION_TYPE_TLS = "tls";

    private String defaultSender;
    private String defaultSenderName;
    private String encryptionType;
    private boolean alwaysUseDefaultSender;

    @Value("${mail.server.host}")
    @Override
    public void setHost(String host) {
        super.setHost(host);
    }

    @Value("${mail.username}")
    @Override
    public void setUsername(String username) {
        super.setUsername(username);
    }

    @Value("${mail.password}")
    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    public String getDefaultSender() {
        return defaultSender;
    }

    @Value("${mail.default.sender}")
    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }
    
    @Value("${mail.server.port}")
    public void setPort(String port) {
        super.setPort(Integer.valueOf(port));
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    @Value("${mail.encryption.type}")
    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
        if (ENCRYPTION_TYPE_TLS.equalsIgnoreCase(encryptionType)){
            Properties javaMailProperties = new Properties();
            javaMailProperties.setProperty("mail.smtp.auth", "true");
            javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
            setJavaMailProperties(javaMailProperties);
        }else if (ENCRYPTION_TYPE_SSL.equalsIgnoreCase(encryptionType)){
            Properties javaMailProperties = new Properties();
            javaMailProperties.setProperty("mail.smtp.auth", "true");
            javaMailProperties.put("mail.smtp.socketFactory.port", getPort());
            javaMailProperties.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            setJavaMailProperties(javaMailProperties);
        }
    }

    @Value("${mail.default.sender.name}")
    public void setDefaultSenderName(String defaultSenderName) {
        this.defaultSenderName = defaultSenderName;        
    }
    public String getDefaultSenderName() {
        return defaultSenderName;        
    }    

    @Value("${mail.always.use.default.sender:false}")
    public void setAlwaysUseDefaultSender(boolean alwaysUseDefaultSender) {
        this.alwaysUseDefaultSender = alwaysUseDefaultSender;        
    }
    /**
     * Флаг того, что надо использовать отправителя по умолчанию всегда
     * @return
     */
    public boolean isAlwaysUseDefaultSender() {
        return alwaysUseDefaultSender;        
    }    

}
