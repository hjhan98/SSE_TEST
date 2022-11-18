package kr.hjhan.sse.test.config;

import kr.hjhan.sse.test.config.auth.JwtAccessDeniedHandler;
import kr.hjhan.sse.test.config.auth.JwtAuthenticationEntryPoint;
import kr.hjhan.sse.test.config.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.logging.Filter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfiguration {
    private static final String[] AUTH_WHITELIST = {
            "/resources/**", "/favicon.ico", "/webjars/**", "/*/signin", "/*/exceptions/**", "/", "/*/prod/**"
    };

    private final Filter loggingFilter;
    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 참고
     * CSRF 공격(Cross Site Request Forgery)은 웹 어플리케이션 취약점 중 하나로 인터넷 사용자(희생자)가 자신의 의지와는 무관하게 공격자가 의도한 행위(수정, 삭제, 등록 등)를 특정 웹사이트에 요청하게 만드는 공격
     */
    public void configure(HttpSecurity http) throws Exception{
        http
                .httpBasic().disable() // 로그인 폼 redirect disalble
                .csrf().disable()      // token 방식임으로 csrf disable
                .exceptionHandling()   //예외처리를 위한 코드 지정
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401 401 UnAuthorized : Jwt가 오지 않은 경우
                .accessDeniedHandler(jwtAccessDeniedHandler)    // 403 Forbidden : Jwt가 왔지만 권한이 다른 경우
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt 인증이므로 세션 사용하지 않음.
                .and()
                .authorizeRequests(authorize -> {

                })


    }
}
