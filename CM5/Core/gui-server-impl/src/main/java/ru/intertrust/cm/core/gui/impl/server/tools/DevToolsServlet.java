package ru.intertrust.cm.core.gui.impl.server.tools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DevToolsServlet", urlPatterns = { "/dev" }, asyncSupported = true)
public class DevToolsServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();

        out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.print("<html>");
        out.print("<head><title>");
        out.print("Инструменты разработчика");
        out.print("</title>");
        out.print("<meta charset='utf-8'>");
        out.print("<body>");
        out.print("<div><a href=\"" + req.getContextPath() + "/remote/service/query\">Отладка запросов</a></div>");
        out.print("<div><a href=\"" + req.getContextPath() + "/remote/service/freemarker\">Отладка шаблонов FreeMarker</a></div>");
        out.print("<div><a href=\"" + req.getContextPath() + "/remote/service/check-crypto-plugin\">Проверка плагина КриптоПро</a></div>");
        out.print("<div><a href=\"" + req.getContextPath() + "/af5-services/globalcache/ping/1000\">Проверка JMS подсистемы кластера</a></div>");
        out.print("<div><a href=\"" + req.getContextPath() + "/af5-services/globalcache/check/1000\">Проверка распределенной блокировки</a></div>");
        out.print("</body>");
        out.print("</html>");
    }
}
