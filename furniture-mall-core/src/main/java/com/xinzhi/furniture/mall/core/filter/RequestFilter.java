package com.xinzhi.furniture.mall.core.filter;

import com.xinzhi.furniture.mall.core.util.IpUtil;
import jodd.io.StreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Configuration
@WebFilter
public class RequestFilter implements Filter {

    private static Log logger = LogFactory.getLog(RequestFilter.class);

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String IP = "ip";
    public static final String URI = "uri";
    public static final String HOST = "host";
    public static final String TOKEN = "token";

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("INFO: RequestFilter---Init");
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        long start = System.currentTimeMillis();

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();

        String uuid = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, uuid);
        MDC.put(IP, IpUtil.getIpAddr(request));
        MDC.put(URI, uri);
        MDC.put(HOST, request.getHeader("host"));
        MDC.put(TOKEN, request.getHeader("X-Litemall-Token"));
//
//        HttpSession session = request.getSession(false);
//        if(session != null) {
//            MDC.put(USER_ID, session.getAttribute(UserConstants.USER_ID) + "");
//        }

//        logger.info("IP: " + IPUtils.getIpAddress(request) + " Method: " + request.getMethod() + " RequestURI: " + uri);
        Map<String, String[]> parameterMap = request.getParameterMap();
        if(parameterMap != null && !parameterMap.isEmpty()) {
            StringBuffer params = new StringBuffer();
            parameterMap.forEach((o, p) -> {
                String v = "";
                for(String s : p) {
                    v += s + ", ";
                }
                params.append(o + " = " + v);
            });
            logger.info("RequestParams: " + params.toString());
        }
        BodyReaderHttpServletRequestWrapper wrapper = new BodyReaderHttpServletRequestWrapper(request);
        String body = getBodyString(wrapper);
        if(!StringUtils.isEmpty(body)) {
            logger.info("RequestBody: " + body);
        }

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("ReqId", uuid);
        response.setDateHeader("Expires", 0);

        filterChain.doFilter(wrapper, response);

        long time = Instant.now().toEpochMilli() - start;
        if(time > 300) {
            logger.info("Elapsed time: " + time + "ms");
        }
        MDC.clear();
    }


    @Override
    public void destroy() {
        logger.info("INFO: RequestFilter---Destroy");
    }

    public static String getBodyString(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] body;

        public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            body = StreamUtil.readBytes(request.getInputStream());
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener arg0) {

                }
            };
        }
    }

}
