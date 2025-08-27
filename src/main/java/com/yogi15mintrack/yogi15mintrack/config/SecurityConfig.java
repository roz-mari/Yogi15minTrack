package com.yogi15mintrack.yogi15mintrack.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtService;
import com.yogi15mintrack.yogi15mintrack.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.yogi15mintrack.yogi15mintrack.users.UserServiceImpl;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

   // private final UserServiceImpl userService;
    //public SecurityConfig(UserServiceImpl userService) { // инжект интерфейса
    //    this.userService = userService;
   // }


    //public SecurityConfig(UserService userService) {
     //  this.userService = userService;
    //}


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    //public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserService userService) {
    //    return new JwtAuthFilter(jwtService, userService);
    //}
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService,
                                       org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }


    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String body = """
                {"status":401,"message":"Unauthorized: %s","path":"%s"}
                """.formatted(exception.getMessage(), request.getRequestURI());
            response.getWriter().write(body);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   AuthenticationEntryPoint entryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sessions", "/sessions/today").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sessions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/sessions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sessions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/completed").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/completed", "/completed/today", "/streaks").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/videos/upload").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/videos/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}