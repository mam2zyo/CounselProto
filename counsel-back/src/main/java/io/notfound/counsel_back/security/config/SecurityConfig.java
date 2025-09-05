package io.notfound.counsel_back.security.config;

import io.notfound.counsel_back.security.filter.JwtAuthenticationFilter;
import io.notfound.counsel_back.security.service.OAuth2UserService;
import io.notfound.counsel_back.security.handler.OAuth2LoginSuccessHandler;
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

/**
 * Spring Security의 전반적인 보안 설정을 담당하는 클래스입니다.
 * JWT 필터, OAuth2 로그인, CORS 등을 설정합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    /**
     * HTTP 보안 필터 체인을 구성하는 빈(Bean)입니다.
     *
     * @param http HttpSecurity 객체
     * @return 설정된 SecurityFilterChain 객체
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 관리 전략을 STATELESS(세션 사용 안 함)로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 일반 로그인 및 회원가입 관련 URL 접근 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        // OAuth2 로그인 관련 URL 접근 허용
                        .requestMatchers(HttpMethod.GET, "/oauth2/authorization/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/login/oauth2/code/**").permitAll()
                        // 토큰 재발급 URL 접근 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chat/**").permitAll()
                        // 그 외 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // OAuth2 인증 후 사용자 정보를 처리하는 서비스 지정
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        // 로그인 성공 핸들러 지정
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        return http.build();
    }

    /**
     * 비밀번호를 안전하게 암호화하기 위한 PasswordEncoder 빈을 등록합니다.
     *
     * @return BCryptPasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Request Sharing) 설정 빈입니다.
     * 프론트엔드와 백엔드 간의 안전한 통신을 위해 허용할 출처, 메서드 등을 정의합니다.
     *
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // 모든 HTTP 메서드(GET, POST, PUT, DELETE 등) 허용
        configuration.setAllowedMethods(List.of("*"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));
        // 자격 증명(쿠키, HTTP 인증 등) 사용 허용
        configuration.setAllowCredentials(true);
        // CORS 설정을 URL 패턴에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
