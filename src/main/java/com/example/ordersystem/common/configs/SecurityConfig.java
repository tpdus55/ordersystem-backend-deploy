package com.example.ordersystem.common.configs;

import com.example.ordersystem.common.auth.JwtTokenFilter;
import com.example.ordersystem.common.exception.JwtAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    @Autowired
    public SecurityConfig(JwtTokenFilter jwtTokenFilter, JwtAuthenticationHandler jwtAuthenticationHandler) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtAuthenticationHandler = jwtAuthenticationHandler;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c->c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(a->a.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e-> e.authenticationEntryPoint(jwtAuthenticationHandler))
                .authorizeHttpRequests

        (a->a.requestMatchers(
                "/member/create",
                "/member/doLogin",
                "/product/list",
                "/member/refresh-at",
//                swagger사용을 위한 인증예외처리
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/health",
                "/swagger-ui.html")

                .permitAll().anyRequest().authenticated())
                .build();
    }

    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
//        허용가능한 도메인 목록 설정
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000","http://localhost:3001","https://www.tpdus.shop"));
//        모든 HTTP메서드(GET,POST,OPTIONS 등) 허용
        configuration.setAllowedMethods(Arrays.asList("*"));
//        모든 헤더요소(Authorization, Content-Type 등) 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
//        자격증명을 허용
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        모든 url패턴에 대해 위 cors정책을 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
