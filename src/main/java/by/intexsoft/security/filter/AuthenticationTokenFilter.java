package by.intexsoft.security.filter;

import by.intexsoft.service.TokenAuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Main validation filter
 */
public class AuthenticationTokenFilter extends GenericFilterBean {

    private final TokenAuthenticationService authenticationService;

    public AuthenticationTokenFilter(TokenAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Validate income user with token
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Authentication authentication = authenticationService.authenticate(httpRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
