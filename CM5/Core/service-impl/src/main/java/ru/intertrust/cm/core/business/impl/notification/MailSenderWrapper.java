package ru.intertrust.cm.core.business.impl.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Обертка для @{see JavaMailSenderImpl}.
 * @author atsvetkov
 *
 */
public class MailSenderWrapper extends JavaMailSenderImpl {

    @Value("${mail.default.sender}")
    private String defaultSender;

    public String getHost() {
        return super.getHost();
    }

    @Value("${mail.server.host}")
    public void setHost(String host) {
        super.setHost(host);
    }

    public String getUsername() {
        return super.getUsername();
    }

    @Value("${mail.username}")
    public void setUsername(String username) {
        super.setUsername(username);
    }

    public String getPassword() {
        return super.getPassword();
    }

    @Value("${mail.password}")
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

}
