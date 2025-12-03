package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    @Value("${server.port:8080}")
    private int port;

    @Value("${spring.devtools.livereload.enabled:false}")
    private boolean livereload;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        int status = response.getStatus();
        String color;
        if (status >= 500) {
            color = RED;
        } else if (status >= 400) {
            color = YELLOW;
        } else if (status >= 200) {
            color = GREEN;
        } else {
            color = CYAN;
        }

        // Ambil asal kode dari stacktrace
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement origin = Arrays.stream(stack)
                .filter(s -> s.getClassName().startsWith("org.delcom"))
                .findFirst()
                .orElse(stack[stack.length - 1]);
        String originInfo = origin.getClassName() + "." + origin.getMethodName() + ":" + origin.getLineNumber();

        String remoteAddr = request.getRemoteAddr();

        String log = String.format(
                "%s%-6s %s %d %dms%s [%s] from %s",
                color,
                request.getMethod(),
                request.getRequestURI(),
                status,
                duration,
                RESET,
                originInfo,
                remoteAddr);

        if (!request.getRequestURI().startsWith("/.well-known")) {
            System.out.println(log);
        }
    }
}
