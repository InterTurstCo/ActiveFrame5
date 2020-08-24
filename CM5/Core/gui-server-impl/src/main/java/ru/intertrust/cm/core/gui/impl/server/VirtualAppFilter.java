package ru.intertrust.cm.core.gui.impl.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * Created by Ravil on 05.12.2017.
 */
public class VirtualAppFilter implements Filter {
    private final static String METHOD_POST = "POST";
    public final static String APP_NAME = "Application-Name";
    public final static String ENCODING_UTF_8 = "UTF-8";
    @EJB
    private ConfigurationService configurationService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        if(businessUniverseConfig.getBaseUrlConfig() != null && businessUniverseConfig.getBaseUrlConfig().getValue() != null
                && ((HttpServletRequest) servletRequest).getRequestURL().toString().contains("/" + businessUniverseConfig.getBaseUrlConfig().getValue()+"/")
                && !((HttpServletRequest) servletRequest).getRequestURL().toString().contains("Login.html")
                )
        {
            servletRequest.setAttribute(APP_NAME, "/" + businessUniverseConfig.getBaseUrlConfig().getValue());
		}

		final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		if (httpRequest.getMethod().equalsIgnoreCase(METHOD_POST) 
				&& businessUniverseConfig.getBaseUrlConfig() != null
				&& businessUniverseConfig.getBaseUrlConfig().getValue() != null
				&& httpRequest.getRequestURI().startsWith(httpRequest.getContextPath() + "/" + businessUniverseConfig.getBaseUrlConfig().getValue())) {


            XSSRequestWrapper wrappedRequest = new XSSRequestWrapper(
                    (HttpServletRequest) servletRequest);
            String body = IOUtils.toString(wrappedRequest.getReader());
            wrappedRequest.resetInputStream(body.replace("/" + businessUniverseConfig.getBaseUrlConfig().getValue(), StringUtils.EMPTY).toString().getBytes(ENCODING_UTF_8));
            filterChain.doFilter(wrappedRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }

    class XSSRequestWrapper extends HttpServletRequestWrapper {


        private byte[] rawData;
        private HttpServletRequest request;
        private ResettableServletInputStream servletStream;

        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
            this.servletStream = new ResettableServletInputStream();
        }


        public void resetInputStream(byte[] newRawData) {
            servletStream.stream = new ByteArrayInputStream(newRawData);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return new BufferedReader(new InputStreamReader(servletStream));
        }

        private class ResettableServletInputStream extends ServletInputStream {

            private InputStream stream;

            @Override
            public int read() throws IOException {
                return stream.read();
            }
        }
    }
}
