package ru.intertrust.cm.remoteclient.idp;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.keycloak.common.util.Base64;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestIdp extends ClientBase {

    public static void main(String[] args) {
        try {
            TestIdp test = new TestIdp();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://localhost:8080/cm-sochi/execAction");
        post.addParameter("beanName", "testKeycloakAdmin");
        post.addRequestHeader("Authorization", Base64.encodeBytes((user + ":" + password).getBytes()));
        int statusCode = client.executeMethod(post);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + post.getStatusLine());
        }else{
            System.out.println("Method OK");
        }
    }
}
