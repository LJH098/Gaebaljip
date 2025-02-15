package com.gaebaljip.exceed.security.config;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gaebaljip.exceed.security.domain.JwtManager;
import com.gaebaljip.exceed.security.domain.JwtResolver;
import com.gaebaljip.exceed.security.exception.JwtAccessDeniedHandler;
import com.gaebaljip.exceed.security.exception.JwtAuthenticationPoint;
import com.gaebaljip.exceed.security.filter.JwtAuthenticationFilter;
import com.gaebaljip.exceed.security.service.MemberDetailService;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtManager jwtManager;
    private final JwtResolver jwtResolver;
    private final MemberDetailService memberDetailService;
    private final JwtAuthenticationPoint jwtAuthenticationPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // httpBasic, csrf, formLogin, rememberMe, logout, session disable
        http.httpBasic()
                .disable()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .rememberMe()
                .disable()
                .logout()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationPoint)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .headers()
                .frameOptions()
                .sameOrigin();

        // 요청에 대한 권한 설정
        http.authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest)
                .permitAll()
                .antMatchers(HttpMethod.GET, "/v1/members/checked/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/v1/members/email/checked")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/v1/members")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/v1/auth/login")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/actuator/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/v1/health")
                .permitAll()
                .anyRequest()
                .authenticated();

        // jwt filter 설정
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtManager, jwtResolver, memberDetailService),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(
                Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .antMatchers("/docs/api-doc.html")
                        .antMatchers("/favicon.*", "/*/icon-*")
                        .antMatchers(
                                "/swagger-resources/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api-docs/**",
                                "/api-docs")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
