package ru.intertrust.cm.core.business.api.dto.crypto;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class SignerInfo implements Dto{
    private static final long serialVersionUID = 7203533747895495733L;
    private boolean valid;
    private String name;
    private String certificateId;
    private Date certificateValidFrom;
    private Date certificateValidTo;
    private String error;
    private Date signDate;
    private String subject;
    private String issuer;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCertificateId() {
        return certificateId;
    }
    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }
    public Date getCertificateValidFrom() {
        return certificateValidFrom;
    }
    public void setCertificateValidFrom(Date certificateValidFrom) {
        this.certificateValidFrom = certificateValidFrom;
    }
    public Date getCertificateValidTo() {
        return certificateValidTo;
    }
    public void setCertificateValidTo(Date certificateValidTo) {
        this.certificateValidTo = certificateValidTo;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    public Date getSignDate() {
        return signDate;
    }
    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
