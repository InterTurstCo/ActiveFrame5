package ru.intertrust.cm.core.service.it;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class LoginPasswordCallbackHandler implements CallbackHandler{
  private String m_login = null;
  private String m_password = null;

  public LoginPasswordCallbackHandler(String login, String password) {
    m_login = login;
    m_password = password;
  }

  public void handle(Callback[] callbacks)
      throws IOException, UnsupportedCallbackException {
    for (int i = 0; i < callbacks.length; i++) {

      if (callbacks[i] instanceof NameCallback) {
        NameCallback nc = (NameCallback) callbacks[i];
        nc.setName(m_login);
      }
      else if (callbacks[i] instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) callbacks[i];
        pc.setPassword(m_password.toCharArray());
      }
    }
  }
}
