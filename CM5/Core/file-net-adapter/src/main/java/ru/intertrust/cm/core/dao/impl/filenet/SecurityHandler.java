package ru.intertrust.cm.core.dao.impl.filenet;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import ru.intertrust.cm.core.model.FatalException;

/**
 * Класс необходимый для аутентификации на сервере filenet
 * @author larin
 *
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {
    private static String WS_SECEXT = "http://docs.oasis-open.org/wss/" +
            "2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private static String WS_SECUTILITY = "http://docs.oasis-open.org/wss/" +
            "2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private static String WS_USER_TOKEN_PROFILE = "http://docs.oasis-open.org" +
            "/wss/2004/01/oasis-200401-wss-username-token-profile-1.0" +
            "#PasswordText";

    private String login;
    private String password;

    public SecurityHandler(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean handleMessage(SOAPMessageContext messageContext) {
        if ((Boolean) messageContext.get(
                MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                GregorianCalendar now = new GregorianCalendar();
                GregorianCalendar fiveMinutesLater = new GregorianCalendar();
                fiveMinutesLater.add(GregorianCalendar.MINUTE, 5);
                XMLGregorianCalendar xmlNow =
                        factory.newXMLGregorianCalendar(now);
                XMLGregorianCalendar xmlFiveMinutesLater =
                        factory.newXMLGregorianCalendar(fiveMinutesLater);
                String xmlNowDate = xmlNow.toXMLFormat().substring(0,
                        xmlNow.toXMLFormat().length() - 6) + "Z";
                String xmlFiveMinutesLaterDate = xmlFiveMinutesLater
                        .toXMLFormat().substring(0,
                                xmlFiveMinutesLater.toXMLFormat().length() - 6) + "Z";
                SOAPMessage msg = messageContext.getMessage();
                SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
                SOAPHeader soapHeader;
                if (envelope.getHeader() != null) {
                    soapHeader = envelope.getHeader();
                } else {
                    soapHeader = envelope.addHeader();
                }
                SOAPHeaderElement headerElement = soapHeader.addHeaderElement(
                        new QName(WS_SECEXT, "Security", "wsse"));
                headerElement.setMustUnderstand(true);
                SOAPElement timestamp = headerElement.addChildElement(
                        new QName(WS_SECUTILITY, "Timestamp", "wsu"));
                timestamp.addAttribute(
                        new QName(WS_SECUTILITY, "Id", "wsu"), "Timestamp");
                SOAPElement created = timestamp.addChildElement(
                        new QName(WS_SECUTILITY, "Created", "wsu"));
                created.addTextNode(xmlNowDate);
                SOAPElement expired = timestamp.addChildElement(
                        new QName(WS_SECUTILITY, "Expired", "wsu"));
                expired.addTextNode(xmlFiveMinutesLaterDate);
                SOAPElement usernameToken = headerElement.addChildElement(
                        new QName(WS_SECEXT, "UsernameToken", "wsse"));
                SOAPElement username = usernameToken.addChildElement(
                        new QName(WS_SECEXT, "Username", "wsse"));
                username.addTextNode(login);
                SOAPElement passwordEl = usernameToken.addChildElement(
                        new QName(WS_SECEXT, "Password", "wsse"));
                passwordEl.addAttribute(new QName("Type"), WS_USER_TOKEN_PROFILE);
                passwordEl.addTextNode(password);
            } catch (Exception ex) {
                throw new FatalException("Error handle messfge", ex);
            }
        }

        return true;
    }

    public Set<QName> getHeaders() {
        return new HashSet<QName>();
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
        return;
    }
}
