package io.hhplus.concert.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.concert.user.usecase.CheckTokenEnrolledUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationHeaderFilter extends BasicAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CheckTokenEnrolledUseCase checkTokenEnrolledUseCase;

    public AuthorizationHeaderFilter(
            AuthenticationManager authenticationManager,
            CheckTokenEnrolledUseCase checkTokenEnrolledUseCase) {
        super(authenticationManager);
        this.checkTokenEnrolledUseCase = checkTokenEnrolledUseCase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        String authorizationHeader = request.getHeader("Authorization");

        if (request.getRequestURI().matches("/api/1.0/users/\\d+/tokens")) {
            chain.doFilter(request, response);
            return;
        }

        if (authorizationHeader == null) {
            sendJsonErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    "Authorization header is missing"
            );
            return;
        }

        CheckTokenEnrolledUseCase.Output output = checkTokenEnrolledUseCase.execute(
                new CheckTokenEnrolledUseCase.Input(
                        authorizationHeader
                )
        );
        if (!output.isAuthenticated()) {
            sendJsonErrorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden",
                    "대기 순서: " + output.rank()
            );
            return;
        }

        logger.info("Filter passed, proceeding to controller.");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authorizationHeader, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private void sendJsonErrorResponse(
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {

        logger.info("Setting response status to: " + status);

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("error", error);
        jsonMap.put("message", message);

        String jsonResponse = new ObjectMapper().writeValueAsString(jsonMap);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        response.getWriter().close();
    }
}
