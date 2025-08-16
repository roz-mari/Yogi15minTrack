package com.yogi15mintrack.yogi15mintrack.security;

import com.yogi15mintrack.yogi15mintrack.users.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        Set<String> exactExclusions = Set.of(
                "/auth/register",
                "/auth/login",
                "/swagger-ui.html"
        );

        List<String> prefixExclusions = List.of(
                "/v3/api-docs/",
                "/swagger-ui/",
                "/swagger-resources/",
                "/webjars/"
        );

        if (exactExclusions.contains(path)) return true;
        for (String prefix : prefixExclusions) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.isValidToken(token)) {
                throw new AuthenticationCredentialsNotFoundException("Invalid JWT token");
            }

            String username = jwtService.extractUsername(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var domainUser = userService.getByUsername(username);
                var principal = new CustomUserDetail(domainUser);

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized: " + ex.getMessage() + "\"}");
            response.getWriter().flush();
        }
    }
}
