package io.notfound.counsel_back.security.config;

import io.notfound.counsel_back.security.core.JwtAuthenticationFilter;
import io.notfound.counsel_back.auth.service.OAuth2UserService;
import io.notfound.counsel_back.security.core.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 기본 폼 로그인과 httpBasic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(this::configureAuthorization)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(this::configureOAuth2Login);

        return http.build();
    }

    /**
     * 인증/인가 규칙 설정
     */
    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        authorize
                // CORS preflight 요청 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 인증 관련 API - 공개 접근
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup", "/api/auth/refresh").permitAll()

                // OAuth2 로그인 - 공개 접근
                .requestMatchers(HttpMethod.GET, "/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()

                // 테스트용 대화 API - 공개 접근
                .requestMatchers("/api/conversations/**").permitAll()

                // Spring Boot 기본 에러 페이지
                .requestMatchers("/error").permitAll()

                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated();
    }

    /**
     * OAuth2 로그인 설정
     */
    private void configureOAuth2Login(
            org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer<HttpSecurity> oauth2) {
        oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정 - 개발/테스트용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (개발 서버 + file 프로토콜)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "null"));

        // 모든 HTTP 메서드 허용
        configuration.setAllowedMethods(List.of("*"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠키 기반 인증을 위해 credentials 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}