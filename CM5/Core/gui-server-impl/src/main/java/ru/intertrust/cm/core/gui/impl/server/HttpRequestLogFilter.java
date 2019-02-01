package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.api.server.HttpRequestFilterUser;
import ru.intertrust.cm.core.gui.api.server.LoginService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ravil on 12.10.2017.
 */
@SuppressWarnings("restriction")
@Component(value = "httpRequestLogFilter")
public class HttpRequestLogFilter extends CommonsRequestLoggingFilter {
    private static final String EXCLUSIONS_DELIMITER = ",";
    private Long elapsed;
    private Long startTime;
    private long startMem;
    private long diffMem;
    private MemBytes memBytes;
    @Value("${http.request.log.min.time:100}")
    private Integer minTime;

    @Value("${http.request.log.excluded.patterns:#{null}}")
    private String excludePatterns;

    @EJB
    private List<HttpRequestFilterUser> beanList;

    @PostConstruct
    protected void postConstruct () {
        
        try {
            this.getClass().getClassLoader().loadClass("com.sun.management.ThreadMXBean"); // проверим производителя JDK:
        } catch (final ClassNotFoundException e) {                                         // если не Oracle/Sun,
            return;                                                                        // то память считать мы не умеем
        }
        
        final com.sun.management.ThreadMXBean sb = (com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
        
        if (sb.isThreadAllocatedMemorySupported()) {
            
            if (!sb.isThreadAllocatedMemoryEnabled()) {
                sb.setThreadAllocatedMemoryEnabled(true);
            }
            
            this.memBytes = new MemBytes() {

                @Override
                public long getThreadAllocatedBytes () {
                    return sb.getThreadAllocatedBytes(Thread.currentThread().getId());
                }
                
            };
            
        }
        
    }
    
    @Override
    protected void afterRequest(HttpServletRequest request,
                                String message) {

        if (((excludePatterns != null && !isMatchByPattern(request.getRequestURI()))
                || excludePatterns == null)
                &&
                elapsed>=minTime) {
            super.afterRequest(request, message);
        }
    }

    @Override
    protected String createMessage(HttpServletRequest request, String prefix, String suffix) {
        UserCredentials credentials = (UserCredentials) request.getSession().getAttribute(
                LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        String requestString = request.getRequestURL() +
                ((request.getQueryString() != null) ? ("?" + request.getQueryString()):"");
        String loginName = findUser(request);
        String logRecord = String.format("%-10s\t%-10s\t%s\t%-10s\t%s\t%-15s\t%s", elapsed, this.startMem < 0 ? "---" : this.diffMem, request.getMethod(),
                request.getContentLength(),request.getRemoteAddr(),loginName,requestString);
        Cookie[] cookies = request.getCookies();
        return logRecord;
    }

    private Boolean isMatchByPattern(String data) {
        List<Pattern> patterns = new ArrayList<>();
        if (excludePatterns != null) {
            for (String exclusion : excludePatterns.split(EXCLUSIONS_DELIMITER)) {
                patterns.add(Pattern.compile(exclusion));
            }
        }
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(data);
            if (matcher.find()) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        startTime = System.currentTimeMillis();
        this.startMem = this.getThreadAllocatedBytes();

        boolean isFirstRequest = !this.isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;
        if(this.isIncludePayload() && isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        boolean shouldLog = this.shouldLog(requestToUse);

        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            if(shouldLog && !this.isAsyncStarted(requestToUse)) {
                elapsed = System.currentTimeMillis() - startTime;
                this.diffMem = this.getThreadAllocatedBytes() - this.startMem;
                this.afterRequest(requestToUse, createMessage(requestToUse,null,null));
            }

        }
    }

    private String findUser(HttpServletRequest request){
        for(HttpRequestFilterUser b : beanList){
            if(b.getUserName(request)!=null)
                return b.getUserName(request);
        }
        return "NOT_LOGGED";

    }
    
    private long getThreadAllocatedBytes () {
        return this.memBytes == null ? Long.MIN_VALUE : this.memBytes.getThreadAllocatedBytes();
    }
    
    private static interface MemBytes {
        
        long getThreadAllocatedBytes ();
        
    }
    
}
