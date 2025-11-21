package com.school.security.securities.config;

import com.school.security.services.contracts.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.POST, "/auth/login")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/register")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/auth/code")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/users",
                                                "/users/{id}",
                                                "/users/email",
                                                "/users/invitation",
                                                "/users/statistics",
                                                "/users/statistics/technician",
                                                "/users/organisation")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/material", "/material/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/material")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/material/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/invitation")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/users/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.PUT, "/users/pwd", "/users/role")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/users/role")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/directions", "/directions/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/directions/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/directions")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/directions/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/speciality", "/speciality/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/speciality/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/speciality")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/speciality/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/request",
                                                "/request/invitation",
                                                "/request/{id}",
                                                "/request/users/{id}",
                                                "/request/variation-by-years",
                                                "/request/years-possibles",
                                                "/request/statistics")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/request/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/request")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/request/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/interventions",
                                                "/interventions/invitation",
                                                "/interventions/{id}",
                                                "/interventions/variation-by-years",
                                                "/interventions/years-possibles",
                                                "interventions/statistics")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "interventions/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/interventions")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/interventions/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/maintenances",
                                                "/maintenances/{id}",
                                                "/maintenances/variation-by-years",
                                                "/maintenances/years-possibles",
                                                "/maintenances/statistics")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/maintenances/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/maintenances")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/maintenances/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/journals", "/journals/{id}")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/journals")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/journals/{id}")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/stats",
                                                "/stats/trimester",
                                                "/stats/{id}",
                                                "/stats/betweenDates",
                                                "/stats/month")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/stats")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
