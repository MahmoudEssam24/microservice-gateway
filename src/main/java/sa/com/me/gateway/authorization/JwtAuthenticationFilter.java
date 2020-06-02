package sa.com.me.gateway.authorization;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import sa.com.me.core.exception.ErrorMessage;
import sa.com.me.core.exception.ErrorSource;
import sa.com.me.core.exception.NotAuthorizedException;
import sa.com.me.core.util.CoreUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwtHeader}")
    private String HEADER_STRING;

    @Value("${app.jwtTokenPrefix}")
    private String TOKEN_PREFIX;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Uri is: {}", request.getRequestURI().contains("/public/"));
        if (isPublicApi(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                String jwt = getJwtFromRequest(request);
                log.info("Validating authentication for user {}", tokenProvider.getUsernameFromJWT(jwt));

                if (jwt == null) {
                    throw new NotAuthorizedException("User not authorized", "401", "email");
                }
                
                if (StringUtils.hasText(jwt)) {
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    if (tokenProvider.validateToken(jwt, userDetails) && username != null
                            && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authentication = tokenProvider.getAuthentication(jwt,
                                SecurityContextHolder.getContext().getAuthentication(), userDetails);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("User authenticated with token: {}", jwt);
                    }
                }
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ErrorMessage errorMessage = new ErrorMessage();
                ErrorSource errorResponse = new ErrorSource("authoriation", "401",
                        "Unauthorized access, No headers passed");
                errorMessage.setError(errorResponse);
                response.getWriter().write(CoreUtils.convertObjectToJson(errorResponse));
            }
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicApi(HttpServletRequest request) {
        return request.getRequestURI().contains("/public/");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HEADER_STRING);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            String authToken = header.replace(TOKEN_PREFIX, "");
            return authToken;
        }
        return null;
    }
}