package ru.intertrust.cm.core.gui.impl.server.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AF5CheckCryptoServlet", urlPatterns = { "/remote/service/af5-check-crypto-settings" }, asyncSupported = true)
public class AF5CheckCryptoServlet extends HttpServlet {

    @Autowired
    private GlobalServerSettingsService settingsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        Boolean b = settingsService.getBoolean("af5.crypto.enabled");
        out.print(b != null && b.booleanValue() ? "1" : "0");
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // do nothing
    }

}
